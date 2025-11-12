// java
package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.SalesCreateDTO;
import com.pulse.fineflux.domain.SalesResponseDTO;
import com.pulse.fineflux.domain.SalesUpdateDTO;
import com.pulse.fineflux.entity.*;
import com.pulse.fineflux.repository.*;
import com.pulse.fineflux.utill.SaleMatch;
import com.pulse.fineflux.utill.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalesServiceImpl implements SalesService {

    private final SalesRepository salesRepository;
    private final GunInfoRepository gunInfoRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryLogRepository inventoryLogRepository;
    private final FinanceSummaryService financeSummaryService;
    private final SaleHistoryRepository saleHistoryRepository;
    private final CollectionsRepository collectionsRepository;

    // Helper: convert client LocalDateTime (in system default zone) to IST LocalDateTime truncated to seconds
    private LocalDateTime toIst(LocalDateTime clientTs) {
        ZoneId sourceZone = ZoneId.systemDefault();
        return DateTimeUtil.toIst(clientTs, sourceZone).truncatedTo(ChronoUnit.SECONDS);
    }

    // Helper: current IST truncated to seconds
    private LocalDateTime nowIst() {
        return DateTimeUtil.nowLocal();
    }

    @Override
    public SalesResponseDTO createSale(SalesCreateDTO dto) {
        try {
            // If client provided a timestamp, convert it from system default zone to IST;
            // otherwise use current IST
            LocalDateTime istSecond = dto.getDateTime() != null ? toIst(dto.getDateTime()) : nowIst();

            Product product = productRepository.findByOrganizationId(dto.getOrganizationId())
                    .stream()
                    .filter(p -> p.getProductName().trim().equalsIgnoreCase(dto.getProductName().trim()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Product not found: " + dto.getProductName()));

            // find the matching GunInfo when needed below (we update via stream later), no separate gunName variable required

            double opening = gunInfoRepository.findByOrganizationId(dto.getOrganizationId())
                    .stream()
                    .filter(g -> g.getProductName() != null
                            && g.getProductName().trim().equalsIgnoreCase(dto.getProductName().trim())
                            && g.getGuns().trim().equalsIgnoreCase(dto.getGuns().trim()))
                    .findFirst()
                    .map(GunInfo::getCurrentReading)
                    .orElse(0.0);

            double closing = dto.getClosingStock();
            double testing = dto.getTestingTotal();

            double liters = closing - opening - testing;
            if (liters < 0) liters = 0.0;
            float amount = (float) (liters * dto.getPrice());
            if (amount < 0f) amount = 0f;

            BigDecimal currLevel = product.getCurrentLevel() != null ? product.getCurrentLevel() : BigDecimal.ZERO;
            BigDecimal required = BigDecimal.valueOf(liters);
            if (currLevel.compareTo(required) < 0) {
                throw new RuntimeException("create " + currLevel);
            }

            String productDisplay = dto.getProductName().trim();
            String gunsDisplay = dto.getGuns().trim();
            String productNorm = SaleMatch.normalize(productDisplay);
            String gunsNorm = SaleMatch.normalize(gunsDisplay);

            String saleId = java.util.UUID.randomUUID().toString();
            String saleMatchKey = SaleMatch.buildKey(istSecond, productNorm, gunsNorm, dto.getPrice());

            Sales sale = Sales.builder()
                    .saleId(saleId)
                    .organizationId(dto.getOrganizationId())
                    .dateTime(istSecond) // store IST
                    .productName(productNorm)
                    .guns(gunsNorm)
                    .price(dto.getPrice())
                    .saleMatchKey(saleMatchKey)
                    .empId(dto.getEmpId())
                    .openingStock(opening)
                    .closingStock(closing)
                    .testingTotal(testing)
                    .salesInLiters((float) liters)
                    .salesInRupees(amount)
                    .build();

            Sales saved = salesRepository.save(sale);
            financeSummaryService.autoCreateFinanceSummary(saved.getOrganizationId());

            gunInfoRepository.findByOrganizationId(dto.getOrganizationId()).stream()
                    .filter(g -> g.getProductName() != null
                            && g.getProductName().trim().equalsIgnoreCase(saved.getProductName())
                            && g.getGuns().trim().equalsIgnoreCase(saved.getGuns()))
                    .findFirst()
                    .ifPresent(gunInfo -> {
                        gunInfo.setProductName(saved.getProductName());
                        gunInfo.setCurrentReading(saved.getClosingStock());
                        gunInfoRepository.save(gunInfo);
                        log.info("GunInfo '{} / {}' currentReading updated to {} and productName to '{}'",
                                gunInfo.getGuns(), gunInfo.getSerialNumber(), saved.getClosingStock(), saved.getProductName());
                    });

            BigDecimal currProduct = product.getCurrentLevel() != null ? product.getCurrentLevel() : BigDecimal.ZERO;
            BigDecimal decreaseBy = BigDecimal.valueOf(saved.getSalesInLiters());
            BigDecimal updatedProductLevel = currProduct.subtract(decreaseBy);
            if (updatedProductLevel.compareTo(BigDecimal.ZERO) < 0) updatedProductLevel = BigDecimal.ZERO;
            product.setCurrentLevel(updatedProductLevel);
            productRepository.save(product);

            List<Inventory> inventories = inventoryRepository.findAllByOrganizationIdAndProductId(dto.getOrganizationId(), product.getId());
            inventories.stream()
                    .max(Comparator.comparing(Inventory::getLastUpdated))
                    .ifPresent(inventory -> {
                        BigDecimal currInv = inventory.getCurrentLevel() != null ? inventory.getCurrentLevel() : BigDecimal.ZERO;
                        BigDecimal updatedInv = currInv.subtract(decreaseBy);
                        if (updatedInv.compareTo(BigDecimal.ZERO) < 0) updatedInv = BigDecimal.ZERO;

                        BigDecimal priceVal = product.getPrice() != null ? BigDecimal.valueOf(product.getPrice()) : BigDecimal.ZERO;
                        BigDecimal updatedStockValue = priceVal.multiply(updatedInv);

                        inventory.setCurrentLevel(updatedInv);
                        inventory.setStockValue(updatedStockValue);
                        inventoryRepository.save(inventory);

                        LocalDateTime istLogTime = nowIst();

                        InventoryLog logEntry = InventoryLog.builder()
                                .inventoryId(inventory.getInventoryId())
                                .organizationId(inventory.getOrganizationId())
                                .productId(inventory.getProductId())
                                .productName(inventory.getProductName())
                                .totalCapacity(inventory.getTotalCapacity())
                                .stockValue(updatedStockValue)
                                .lastUpdated(istLogTime) // store IST
                                .empId(dto.getEmpId())
                                .currentLevel(updatedInv)
                                .metric(inventory.getMetric())
                                .status(inventory.getStatus())
                                .tankCapacity(inventory.getTankCapacity())
                                .mutationby("inventory sale entry created by " + dto.getEmpId())
                                .build();

                        inventoryLogRepository.save(logEntry);

                        // Save which inventory this sale consumed so we can rollback correctly later
                        try {
                            saved.setInventoryId(inventory.getInventoryId());
                            salesRepository.save(saved);
                        } catch (Exception ex) {
                            log.warn("Failed to save inventoryId on sale {}: {}", saved != null ? saved.getId() : "<null>", ex.getMessage());
                        }
                    });

            return SalesResponseDTO.builder()
                    .id(saved.getId())
                    .saleId(saved.getSaleId())
                    .organizationId(saved.getOrganizationId())
                    .dateTime(saved.getDateTime()) // stored IST, return as-is
                    .productName(saved.getProductName())
                    .guns(saved.getGuns())
                    .empId(saved.getEmpId())
                    .openingStock(saved.getOpeningStock())
                    .closingStock(saved.getClosingStock())
                    .testingTotal(saved.getTestingTotal())
                    .salesInLiters(saved.getSalesInLiters())
                    .price(saved.getPrice())
                    .salesInRupees(saved.getSalesInRupees())
                    .build();

        } catch (Exception e) {
            log.error("Error creating sale: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }

    public Optional<LatestInventoryStatus> getLatestInventoryForProduct(String organizationId, String productId) {
        return inventoryRepository.findAllByOrganizationIdAndProductId(organizationId, productId)
                .stream()
                .max(Comparator.comparing(Inventory::getLastUpdated))
                .map(inv -> new LatestInventoryStatus(inv.getStockValue(), inv.getCurrentLevel()));
    }

    public static class LatestInventoryStatus {
        public final BigDecimal stockValue;
        public final BigDecimal currentLevel;
        public LatestInventoryStatus(BigDecimal stockValue, BigDecimal currentLevel) {
            this.stockValue = stockValue;
            this.currentLevel = currentLevel;
        }
    }

    @Override
    public List<SalesResponseDTO> getSalesByDateRange(String organizationId, LocalDateTime from, LocalDateTime to) {
        try {
            log.info("Fetching sales for orgId={} from={} to={}", organizationId, from, to);
            List<Sales> sales = salesRepository.findByOrganizationIdAndDateTimeBetween(organizationId, from, to);
            log.debug("Found {} sales for orgId={} in date range", sales.size(), organizationId);
            return sales.stream().map(this::toResponse).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching sales by date for orgId={}: {}", organizationId, e.getMessage(), e);
            throw new RuntimeException("Error fetching sales by date range: " + e.getMessage());
        }
    }

    @Override
    public SalesResponseDTO updateSale(String id, SalesUpdateDTO dto) {
        try {
            Sales sale = salesRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Sale not found"));
            sale.setClosingStock(dto.getClosingStock());
            sale.setTestingTotal(dto.getTestingTotal());
            sale.setSalesInLiters(dto.getSalesInLiters());
            sale.setPrice(dto.getPrice());
            sale.setSalesInRupees(dto.getSalesInRupees());
            Sales updated = salesRepository.save(sale);
            return toResponse(updated);
        } catch (Exception e) {
            log.error("Error updating sale id={}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Error updating sale " + e.getMessage());
        }
    }

    @Override
    public SalesResponseDTO getSaleById(String id) {
        return salesRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Sale not found"));
    }

    @Override
    public List<SalesResponseDTO> getAllSales(String organizationId) {
        return salesRepository.findByOrganizationId(organizationId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    @Override
    public void deleteSale(String saleMongoId, String employeeId) {
        try {
            Sales sale = salesRepository.findById(saleMongoId)
                    .orElseThrow(() -> new RuntimeException("Sale not found"));
            String orgId = sale.getOrganizationId();
            String productName = sale.getProductName() != null ? sale.getProductName().trim().toLowerCase() : "";
            String guns = sale.getGuns() != null ? sale.getGuns().trim().toLowerCase() : "";
            double addBackLiters = sale.getSalesInLiters();
            String saleId = sale.getSaleId();

            // Delete related Collections
            Long deletedCount = 0L;
            try {
                deletedCount = collectionsRepository.deleteByOrganizationIdAndSaleId(orgId, saleId);
            } catch (UnsupportedOperationException ex) {
                List<Collections> rel = collectionsRepository.findAllByOrganizationIdAndProductNameAndGuns(orgId, productName, guns);
                rel.stream()
                        .filter(c -> saleId != null && saleId.equals(c.getSaleId()))
                        .forEach(c -> collectionsRepository.deleteById(c.getId()));
                deletedCount = (long) rel.size();
            }
            log.info("Deleted {} collection(s) for orgId={} saleId={}", deletedCount, orgId, saleId);

            // Always create a NEW SaleHistory audit entry for this delete and store IST
            LocalDateTime istDeleteTime = nowIst();

            SaleHistory deletedRecord = new SaleHistory();
            deletedRecord.setId(null); // ensure insertion as a new doc
            deletedRecord.setSaleId(saleId);
            deletedRecord.setOrganizationId(orgId);
            deletedRecord.setDateTime(istDeleteTime); // store IST
            deletedRecord.setProductName(productName);
            deletedRecord.setGuns(guns);
            deletedRecord.setEmpId(employeeId);
            deletedRecord.setOpeningStock(sale.getOpeningStock());
            deletedRecord.setClosingStock(sale.getClosingStock());
            deletedRecord.setTestingTotal(sale.getTestingTotal());
            deletedRecord.setSalesInLiters(sale.getSalesInLiters());
            deletedRecord.setPrice(sale.getPrice());
            deletedRecord.setSalesInRupees(sale.getSalesInRupees());
            deletedRecord.setMutationby("sale_delete");
            deletedRecord.setLastUpdated(nowIst()); // store IST
            // use insert to force new doc creation
            saleHistoryRepository.insert(deletedRecord);
            log.info("Inserted SaleHistory delete audit for saleId={}, mongoId={}", saleId, saleMongoId);

            // Update GunInfo (revert readings)
            gunInfoRepository.findByOrganizationId(orgId).stream()
                    .filter(g -> g.getProductName() != null
                            && g.getProductName().trim().equalsIgnoreCase(productName)
                            && g.getGuns().trim().equalsIgnoreCase(guns))
                    .findFirst()
                    .ifPresent(gunInfo -> {
                        double currReading = gunInfo.getCurrentReading();
                        double newReading = currReading - addBackLiters;
                        if (newReading < 0) newReading = 0.0;
                        gunInfo.setCurrentReading(newReading);
                        gunInfoRepository.save(gunInfo);
                        log.info("GunInfo '{}' currentReading decremented by {} to {} for sale delete",
                                gunInfo.getGuns(), addBackLiters, newReading);
                    });

            // Update Product currentLevel (add back liters)
            Product product = productRepository.findByOrganizationId(orgId).stream()
                    .filter(p -> p.getProductName().equalsIgnoreCase(productName))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            BigDecimal productLevel = product.getCurrentLevel() != null ? product.getCurrentLevel() : BigDecimal.ZERO;
            product.setCurrentLevel(productLevel.add(BigDecimal.valueOf(addBackLiters)));
            productRepository.save(product);

            // Restore only the inventory that was affected by this sale (if known), otherwise use latest
            List<Inventory> inventories = inventoryRepository.findAllByOrganizationIdAndProductId(orgId, product.getId());
            Inventory targetInv = null;
            if (sale.getInventoryId() != null) {
                targetInv = inventoryRepository.findById(sale.getInventoryId()).orElse(null);
            }
            if (targetInv == null && !inventories.isEmpty()) {
                targetInv = inventories.stream().max(Comparator.comparing(Inventory::getLastUpdated)).orElse(inventories.get(0));
            }

            if (targetInv != null) {
                BigDecimal invLevel = targetInv.getCurrentLevel() != null ? targetInv.getCurrentLevel() : BigDecimal.ZERO;
                BigDecimal updatedInvLevel = invLevel.add(BigDecimal.valueOf(addBackLiters));
                targetInv.setCurrentLevel(updatedInvLevel);

                BigDecimal price = product.getPrice() != null ? BigDecimal.valueOf(product.getPrice()) : BigDecimal.ZERO;
                targetInv.setStockValue(price.multiply(targetInv.getCurrentLevel()));
                inventoryRepository.save(targetInv);

                // Create a NEW InventoryLog entry for the target inventory after rollback (store IST)
                LocalDateTime istDeleteTimeLog = nowIst();

                InventoryLog deleteLog = InventoryLog.builder()
                        .id(null) // ensure new document
                        .inventoryId(targetInv.getInventoryId())
                        .organizationId(targetInv.getOrganizationId())
                        .productId(targetInv.getProductId())
                        .productName(targetInv.getProductName())
                        .lastUpdated(istDeleteTimeLog) // store IST
                        .empId(employeeId)
                        .currentLevel(targetInv.getCurrentLevel())   // updated level (after addBack)
                        .stockValue(targetInv.getStockValue())       // updated stockValue
                        .metric(targetInv.getMetric())
                        .status(targetInv.getStatus())
                        .tankCapacity(targetInv.getTankCapacity())
                        .mutationby("sale_delete")
                        .build();

                inventoryLogRepository.insert(deleteLog);
                log.info("Inserted inventory delete log for product={} inventoryId={}", productName, targetInv.getInventoryId());

                // Previously we reconciled ALL remaining inventories here which created multiple logs and
                // caused incorrect 'sale_delete - reconciled' entries in the DB. Remove that behaviour.

                // End of target inventory rollback handling
            }

            // Finally delete the sale
            salesRepository.deleteById(saleMongoId);
            log.info("Sale deleted and rollback/audit applied for saleId={}", saleId);

            // Recalculate finance summary
            try {
                log.info("Calling financeSummaryService.autoCreateFinanceSummary after sale deletion for orgId={}", orgId);
                financeSummaryService.autoCreateFinanceSummary(orgId);
                log.info("FinanceSummary auto-updated after sale deletion for orgId={}", orgId);
            } catch (Exception fsEx) {
                log.error("FinanceSummary auto-update failed after sale delete for orgId={}: {}", orgId, fsEx.getMessage(), fsEx);
            }

        } catch (Exception e) {
            log.error("Error deleting sale id={}: {}", saleMongoId, e.getMessage(), e);
            throw new RuntimeException("Error deleting sale " + e.getMessage());
        }
    }
    private SalesResponseDTO toResponse(Sales sale) {
        // Stored timestamps are now in IST. Return as-is for UI.
        LocalDateTime istStored = sale.getDateTime();

        return SalesResponseDTO.builder()
                .id(sale.getId())
                .saleId(sale.getSaleId())
                .organizationId(sale.getOrganizationId())
                .dateTime(istStored)
                .productName(sale.getProductName())
                .guns(sale.getGuns())
                .empId(sale.getEmpId())
                .openingStock(sale.getOpeningStock())
                .closingStock(sale.getClosingStock())
                .testingTotal(sale.getTestingTotal())
                .salesInLiters(sale.getSalesInLiters())
                .price(sale.getPrice())
                .salesInRupees(sale.getSalesInRupees())
                .build();
    }
}
