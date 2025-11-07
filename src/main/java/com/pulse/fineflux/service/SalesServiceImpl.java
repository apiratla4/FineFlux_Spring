package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.*;
import com.pulse.fineflux.entity.*;
import com.pulse.fineflux.repository.*;
import com.pulse.fineflux.utill.SaleMatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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

    @Override
    public SalesResponseDTO createSale(SalesCreateDTO dto) {
        try {
            ZoneId sourceZone = ZoneId.systemDefault();
            LocalDateTime clientTs = dto.getDateTime() != null ? dto.getDateTime() : LocalDateTime.now(sourceZone);
// Normalize to IST seconds if you match by IST, but store UTC
            ZonedDateTime clientZdt = clientTs.atZone(sourceZone);
            ZonedDateTime istZdt = clientZdt.withZoneSameInstant(ZoneId.of("Asia/Kolkata")).truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
            ZonedDateTime utcZdt = istZdt.withZoneSameInstant(ZoneId.of("UTC")); // store UTC
            LocalDateTime utcSecond = utcZdt.toLocalDateTime();
            // 2) Product lookup
            Product product = productRepository.findByOrganizationId(dto.getOrganizationId())
                    .stream()
                    .filter(p -> p.getProductName().trim().equalsIgnoreCase(dto.getProductName().trim()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Product not found: " + dto.getProductName())); // keep behavior [web:14]

            // 3) Gun lookup for org+product+gun
            String gunName = gunInfoRepository.findByOrganizationId(dto.getOrganizationId())
                    .stream()
                    .filter(g -> g.getProductName() != null
                            && g.getProductName().trim().equalsIgnoreCase(dto.getProductName().trim())
                            && g.getGuns().trim().equalsIgnoreCase(dto.getGuns().trim()))
                    .findFirst()
                    .map(GunInfo::getGuns)
                    .orElseThrow(() -> new RuntimeException("Gun not found for product: " + dto.getProductName() + " and gun: " + dto.getGuns())); // unchanged [web:14]

            // 4) Opening from GunInfo (never trust DTO.opening)
            double opening = gunInfoRepository.findByOrganizationId(dto.getOrganizationId())
                    .stream()
                    .filter(g -> g.getProductName() != null
                            && g.getProductName().trim().equalsIgnoreCase(dto.getProductName().trim())
                            && g.getGuns().trim().equalsIgnoreCase(dto.getGuns().trim()))
                    .findFirst()
                    .map(GunInfo::getCurrentReading)
                    .orElse(0.0); // unchanged [web:14]

            double closing = dto.getClosingStock();
            double testing = dto.getTestingTotal();

            double liters = closing - opening - testing;
            if (liters < 0) liters = 0.0; // guard [web:14]
            float amount = (float) (liters * dto.getPrice());
            if (amount < 0f) amount = 0f; // guard [web:14]

            // 5) Stock validation
            BigDecimal currLevel = product.getCurrentLevel() != null ? product.getCurrentLevel() : BigDecimal.ZERO;
            BigDecimal required = BigDecimal.valueOf(liters);
            if (currLevel.compareTo(required) < 0) {
                throw new RuntimeException("create " + currLevel); // your original behavior [web:14]
            }

            // 6) Normalize names for storage + keep display for UI
            String productDisplay = dto.getProductName().trim();
            String gunsDisplay = dto.getGuns().trim();
            String productNorm = SaleMatch.normalize(productDisplay); // lower-case, trimmed [web:16]
            String gunsNorm = SaleMatch.normalize(gunsDisplay); // lower-case, trimmed [web:16]

            // 7) Stable saleId and match key
            String saleId = java.util.UUID.randomUUID().toString(); // manual ref id best practice in Mongo [web:12][web:17]
            String saleMatchKey = SaleMatch.buildKey(utcSecond, productNorm, gunsNorm, dto.getPrice()); // second-level key [web:2][web:5]

            // 8) Build and save sale
            Sales sale = Sales.builder()
                    .saleId(saleId)
                    .organizationId(dto.getOrganizationId())
                    .dateTime(utcSecond)
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
                    .build(); // manual reference modeling (avoid DBRef) [web:2][web:3][web:5]

            Sales saved = salesRepository.save(sale); // standard Spring Data Mongo save [web:14]
            financeSummaryService.autoCreateFinanceSummary(saved.getOrganizationId()); // keep your trigger [web:14]

            // 9) Update GunInfo currentReading to closing
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
                    }); // unchanged behavior [web:14]

            // 10) Update product stock
            BigDecimal currProduct = product.getCurrentLevel() != null ? product.getCurrentLevel() : BigDecimal.ZERO;
            BigDecimal decreaseBy = BigDecimal.valueOf(saved.getSalesInLiters());
            BigDecimal updatedProductLevel = currProduct.subtract(decreaseBy);
            if (updatedProductLevel.compareTo(BigDecimal.ZERO) < 0) updatedProductLevel = BigDecimal.ZERO;
            product.setCurrentLevel(updatedProductLevel);
            productRepository.save(product); // unchanged [web:14]

            // 11) Update inventory + log latest
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

                        LocalDateTime utcDeleteTime1 = LocalDateTime.now(ZoneId.of("Asia/Kolkata"))
                                .atZone(ZoneId.of("Asia/Kolkata"))
                                .withZoneSameInstant(ZoneId.of("UTC"))
                                .toLocalDateTime();
                        InventoryLog logEntry = InventoryLog.builder()
                                .inventoryId(inventory.getInventoryId())
                                .organizationId(inventory.getOrganizationId())
                                .productId(inventory.getProductId())
                                .productName(inventory.getProductName())
                                .totalCapacity(inventory.getTotalCapacity())
                                .stockValue(updatedStockValue)
                                .lastUpdated(utcDeleteTime1) // Always current IST time!
                                .empId(dto.getEmpId())
                                .currentLevel(updatedInv)
                                .metric(inventory.getMetric())
                                .status(inventory.getStatus())
                                .tankCapacity(inventory.getTankCapacity())
                                .mutationby("inventory sale entry created by " + dto.getEmpId())
                                .build();

                        inventoryLogRepository.save(logEntry);
                    }); // unchanged semantic, just formatted [web:14]

            // 12) Response uses display names
            return SalesResponseDTO.builder()
                    .id(saved.getId())
                    .saleId(saved.getSaleId())
                    .organizationId(saved.getOrganizationId())
                    .dateTime(saved.getDateTime())
                    .productName(saved.getSaleId() )
                    .guns(saved.getGuns())
                    .empId(saved.getEmpId())
                    .openingStock(saved.getOpeningStock())
                    .closingStock(saved.getClosingStock())
                    .testingTotal(saved.getTestingTotal())
                    .salesInLiters(saved.getSalesInLiters())
                    .price(saved.getPrice())
                    .salesInRupees(saved.getSalesInRupees())
                    .build(); // DTO mapping guideline compatible [web:16]

        } catch (Exception e) {
            log.error("Error creating sale: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage()); // keep existing behavior [web:14]
        }
    }


    // Helper: Get the latest stock value and current level for a product & org
    public Optional<LatestInventoryStatus> getLatestInventoryForProduct(String organizationId, String productId) {
        return inventoryRepository.findAllByOrganizationIdAndProductId(organizationId, productId)
                .stream()
                .max(Comparator.comparing(Inventory::getLastUpdated))
                .map(inv -> new LatestInventoryStatus(inv.getStockValue(), inv.getCurrentLevel()));
    }

    // Helper inner class
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
            // 1. Find the sale being deleted
            Sales sale = salesRepository.findById(saleMongoId)
                    .orElseThrow(() -> new RuntimeException("Sale not found"));
            String orgId = sale.getOrganizationId();

            String productName = sale.getProductName().trim().toLowerCase();
            String guns = sale.getGuns().trim().toLowerCase();
            double addBackLiters = sale.getSalesInLiters();
            String saleId = sale.getSaleId();

            // 2. Delete related Collections
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

            // 3. Audit SaleHistory entry for deletion
            LocalDateTime utcDeleteTime = LocalDateTime.now(ZoneId.of("Asia/Kolkata"))
                    .atZone(ZoneId.of("Asia/Kolkata"))
                    .withZoneSameInstant(ZoneId.of("UTC"))
                    .toLocalDateTime();

            Optional<SaleHistory> existingDel = saleHistoryRepository
                    .findByOrganizationIdAndSaleIdAndMutationby(orgId, saleId, "sale_delete");

            SaleHistory deletedRecord = existingDel.orElseGet(SaleHistory::new);
            existingDel.ifPresent(h -> deletedRecord.setId(h.getId()));

            deletedRecord.setSaleId(saleId);
            deletedRecord.setOrganizationId(orgId);
            deletedRecord.setDateTime(utcDeleteTime);
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
            deletedRecord.setLastUpdated(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            saleHistoryRepository.save(deletedRecord);
            log.info("Recorded SaleHistory delete audit for saleId={}, mongoId={}", saleId, saleMongoId);

            // 4. Update GunInfo
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

            // 5. Update Product's currentLevel
            Product product = productRepository.findByOrganizationId(orgId).stream()
                    .filter(p -> p.getProductName().equalsIgnoreCase(productName))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            BigDecimal productLevel = product.getCurrentLevel() != null ? product.getCurrentLevel() : BigDecimal.ZERO;
            product.setCurrentLevel(productLevel.add(BigDecimal.valueOf(addBackLiters)));
            productRepository.save(product);

            // 6. INSERT ONLY ONE inventory delete log for the product
            List<Inventory> inventories = inventoryRepository.findAllByOrganizationIdAndProductId(orgId, product.getId());
            if (!inventories.isEmpty()) {
                Inventory inv = inventories.get(0); // ONLY ONCE per product!
                String mutationKey = "sale_delete";
                Optional<InventoryLog> existingDelete = inventoryLogRepository.findByInventoryIdAndMutationby(inv.getInventoryId(), mutationKey);

                if (existingDelete.isEmpty()) {
                    InventoryLog deleteLog = InventoryLog.builder()
                            .inventoryId(inv.getInventoryId())
                            .organizationId(inv.getOrganizationId())
                            .productId(inv.getProductId())
                            .productName(inv.getProductName())
                            .lastUpdated(LocalDateTime.now(ZoneId.of("Asia/Kolkata")))
                            .empId(employeeId)
                            .currentLevel(inv.getCurrentLevel())
                            .stockValue(inv.getStockValue())
                            .metric(inv.getMetric())
                            .status(inv.getStatus())
                            .tankCapacity(inv.getTankCapacity())
                            .mutationby(mutationKey)
                            .build();
                    inventoryLogRepository.save(deleteLog);
                    log.info("Inserted inventory delete log for product={} inventoryId={}", productName, inv.getInventoryId());
                } else {
                    InventoryLog logToUpdate = existingDelete.get();
                    logToUpdate.setLastUpdated(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                    logToUpdate.setCurrentLevel(inv.getCurrentLevel());
                    logToUpdate.setStockValue(inv.getStockValue());
                    logToUpdate.setEmpId(employeeId);
                    inventoryLogRepository.save(logToUpdate);
                    log.info("Updated inventory delete log for product={} inventoryId={}", productName, inv.getInventoryId());
                }
            }

            // Always update all other inventories (stock, etc) as required (unchanged)
            for (Inventory inv : inventories) {
                BigDecimal invLevel = inv.getCurrentLevel() != null ? inv.getCurrentLevel() : BigDecimal.ZERO;
                inv.setCurrentLevel(invLevel.add(BigDecimal.valueOf(addBackLiters)));

                BigDecimal price = product.getPrice() != null ? BigDecimal.valueOf(product.getPrice()) : BigDecimal.ZERO;
                inv.setStockValue(price.multiply(inv.getCurrentLevel()));
                inventoryRepository.save(inv);
            }

            // 7. Finally, delete the Sale
            salesRepository.deleteById(saleMongoId);
            log.info("Sale deleted and all relevant rollback/audit applied for saleId={}", saleId);

            // 8. Trigger FinanceSummary recalculation
            try {
                log.info("Calling financeSummaryService.autoCreateFinanceSummary after sale deletion for orgId={}", orgId);
                financeSummaryService.autoCreateFinanceSummary(orgId);
                log.info("FinanceSummary auto-updated after sale deletion for orgId={}", orgId);
            } catch (Exception fsEx) {
                log.error("FinanceSummary auto-update failed after sale delete for orgId={}: {}", orgId, fsEx.getMessage(), fsEx);
            }

        } catch (Exception e) {
            log.error("Error deleting sale id={}: {}", e.getMessage(), e);
            throw new RuntimeException("Error deleting sale " + e.getMessage());
        }
    }


    private SalesResponseDTO toResponse(Sales sale) {
        LocalDateTime utcStored = sale.getDateTime(); // stored in UTC
        LocalDateTime istForUi = null;
        if (utcStored != null) {
            istForUi = utcStored
                    .atZone(ZoneId.of("UTC"))
                    .withZoneSameInstant(ZoneId.of("Asia/Kolkata"))
                    .toLocalDateTime(); // single conversion for UI [web:22]
        }

        return SalesResponseDTO.builder()
                .id(sale.getId())
                .saleId(sale.getSaleId())
                .organizationId(sale.getOrganizationId())
                .dateTime(istForUi) // show IST time derived from stored UTC
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
