package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.*;
import com.pulse.fineflux.entity.*;
import com.pulse.fineflux.repository.*;
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
            LocalDateTime entryDateTime = dto.getDateTime() != null ? dto.getDateTime() : LocalDateTime.now();

            // Find product
            Product product = productRepository.findByOrganizationId(dto.getOrganizationId())
                    .stream()
                    .filter(p -> p.getProductName().trim().equalsIgnoreCase(dto.getProductName().trim()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Product not found: " + dto.getProductName()));

            // Find gun for org+product+gun
            String gunName = gunInfoRepository.findByOrganizationId(dto.getOrganizationId())
                    .stream()
                    .filter(g -> g.getProductName() != null
                            && g.getProductName().trim().equalsIgnoreCase(dto.getProductName().trim())
                            && g.getGuns().trim().equalsIgnoreCase(dto.getGuns().trim()))
                    .findFirst()
                    .map(GunInfo::getGuns)
                    .orElseThrow(() -> new RuntimeException("Gun not found for product: " + dto.getProductName() + " and gun: " + dto.getGuns()));

            // Always get opening from GunInfo (never trust DTO.openingStock!)
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

            // ---------- STOCK VALIDATION ----------
            BigDecimal currLevel = product.getCurrentLevel() != null ? product.getCurrentLevel() : BigDecimal.ZERO;
            BigDecimal required = BigDecimal.valueOf(liters);
            if (currLevel.compareTo(required) < 0) {
                throw new RuntimeException("create " + currLevel);
            }
            // ---------------------------------------

            Sales sale = Sales.builder()
                    .organizationId(dto.getOrganizationId())
                    .dateTime(entryDateTime)
                    .productName(product.getProductName())
                    .guns(gunName)
                    .empId(dto.getEmpId())
                    .openingStock(opening)
                    .closingStock(closing)
                    .testingTotal(testing)
                    .salesInLiters((float) liters)
                    .price(dto.getPrice())
                    .salesInRupees(amount)
                    .build();

            Sales saved = salesRepository.save(sale);
            financeSummaryService.autoCreateFinanceSummary(saved.getOrganizationId());
            // Update GunInfo currentReading after this sale
            gunInfoRepository.findByOrganizationId(dto.getOrganizationId()).stream()
                    .filter(g -> g.getProductName() != null
                            && g.getProductName().trim().equalsIgnoreCase(saved.getProductName().trim())
                            && g.getGuns().trim().equalsIgnoreCase(saved.getGuns().trim()))
                    .findFirst()
                    .ifPresent(gunInfo -> {
                        gunInfo.setProductName(saved.getProductName());
                        gunInfo.setCurrentReading(saved.getClosingStock());
                        gunInfoRepository.save(gunInfo);
                        log.info("GunInfo '{} / {}' currentReading updated to {} and productName to '{}'",
                                gunInfo.getGuns(), gunInfo.getSerialNumber(), saved.getClosingStock(), saved.getProductName());
                    });

            log.info("SaleHistory record created for empId={} and orgId={}", dto.getEmpId(), dto.getOrganizationId());

            try {
                log.info("Calling financeSummaryService.autoCreateFinanceSummary for orgId={} after collection create", saved.getOrganizationId());
               // financeSummaryService.autoCreateFinanceSummary(saved.getOrganizationId());
                log.info("FinanceSummary successfully auto-created for orgId={} after collection create", saved.getOrganizationId());
            } catch (Exception fsEx) {
                log.error("FinanceSummary auto-creation failed for orgId={} after collection create: {}", saved.getOrganizationId(), fsEx.getMessage(), fsEx);
            }

            log.info("Sale created successfully: {}", saved);

            // Update product stock
            BigDecimal currProduct = product.getCurrentLevel() != null ? product.getCurrentLevel() : BigDecimal.ZERO;
            BigDecimal decreaseBy = BigDecimal.valueOf(saved.getSalesInLiters());
            BigDecimal updatedProductLevel = currProduct.subtract(decreaseBy);
            if (updatedProductLevel.compareTo(BigDecimal.ZERO) < 0) updatedProductLevel = BigDecimal.ZERO;
            product.setCurrentLevel(updatedProductLevel);
            productRepository.save(product);

            // Update inventory and log
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
                        inventory.setStockValue(updatedStockValue); // Ensure latest
                        inventoryRepository.save(inventory);

                        InventoryLog logEntry = InventoryLog.builder()
                                .inventoryId(inventory.getInventoryId())
                                .organizationId(inventory.getOrganizationId())
                                .productId(inventory.getProductId())
                                .productName(inventory.getProductName())
                                .totalCapacity(inventory.getTotalCapacity())
                                .stockValue(updatedStockValue)
                                .lastUpdated(LocalDateTime.now(ZoneId.of("Asia/Kolkata")))
                                .empId(dto.getEmpId())
                                .currentLevel(updatedInv)
                                .metric(inventory.getMetric())
                                .status(inventory.getStatus())
                                .tankCapacity(inventory.getTankCapacity())
                                .build();

                        inventoryLogRepository.save(logEntry);

                        log.info("Inventory '{}' currentLevel updated & logged: {} → {} (decreased by {}), stockValue={}",
                                inventory.getProductName(), currInv, updatedInv, decreaseBy, updatedStockValue);
                    });

            return toResponse(saved);

        } catch (Exception e) {
            log.error("❌ Error creating sale for orgId={} productName={}: {}", dto.getOrganizationId(), dto.getProductName(), e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
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
            throw new RuntimeException("Error updating sale: " + e.getMessage());
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
    public void deleteSale(String saleId, String employeeId) {
        try {
            // 1. Find the sale being deleted
            Sales sale = salesRepository.findById(saleId)
                    .orElseThrow(() -> new RuntimeException("Sale not found"));
            String orgId = sale.getOrganizationId();
            String productName = sale.getProductName().trim().toLowerCase();
            String guns = sale.getGuns().trim().toLowerCase();
            double addBackLiters = sale.getSalesInLiters();

            // 2. Delete related Collections by orgId, normalized productName, normalized guns
            List<Collections> collections = collectionsRepository.findAllByOrganizationIdAndProductNameAndGuns(orgId, productName, guns);
            log.info("Found {} collections to delete for saleId={}", collections.size(), saleId);
            collections.forEach(collection -> collectionsRepository.deleteById(collection.getId()));

            // 3. Do NOT delete SaleHistory. Add an audit record with mutationby for deletion.
            SaleHistory deletedRecord = SaleHistory.builder()
                    .organizationId(orgId)
                    .dateTime(ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime())
                    .productName(productName)
                    .guns(guns)
                    .empId(employeeId)
                    .openingStock(sale.getOpeningStock())
                    .closingStock(sale.getClosingStock())
                    .testingTotal(sale.getTestingTotal())
                    .salesInLiters(sale.getSalesInLiters())
                    .price(sale.getPrice())
                    .salesInRupees(sale.getSalesInRupees())
                    .mutationby("sale delete by " + employeeId)
                    .lastUpdated(ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime())
                    .build();
            saleHistoryRepository.save(deletedRecord);
            log.info("Recorded SaleHistory audit log for deleted saleId={}", saleId);

            // 4. Update GunInfo: decrement by salesInLiters
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

            // 6. Update all Inventory records for org+product
            List<Inventory> inventories = inventoryRepository.findAllByOrganizationIdAndProductId(orgId, product.getId());
            for (Inventory inv : inventories) {
                BigDecimal invLevel = inv.getCurrentLevel() != null ? inv.getCurrentLevel() : BigDecimal.ZERO;
                inv.setCurrentLevel(invLevel.add(BigDecimal.valueOf(addBackLiters)));

                BigDecimal price = product.getPrice() != null ? BigDecimal.valueOf(product.getPrice()) : BigDecimal.ZERO;
                inv.setStockValue(price.multiply(inv.getCurrentLevel()));
                inventoryRepository.save(inv);

                // Optionally, insert reversal log
                InventoryLog log = InventoryLog.builder()
                        .inventoryId(inv.getInventoryId())
                        .organizationId(inv.getOrganizationId())
                        .productId(inv.getProductId())
                        .productName(inv.getProductName())
                        .lastUpdated(ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime())
                        .empId(employeeId)
                        .currentLevel(inv.getCurrentLevel())
                        .stockValue(inv.getStockValue())
                        .metric(inv.getMetric())
                        .status(inv.getStatus())
                        .tankCapacity(inv.getTankCapacity())
                        .mutationby("Sale Entry Deleted By :" + employeeId)
                        .build();
                inventoryLogRepository.save(log);
            }

            // 7. Finally, delete the Sale
            salesRepository.deleteById(saleId);
            log.info("Sale deleted and all relevant rollback/audit applied for saleId={}", saleId);

            // ✅ 8. Trigger FinanceSummary recalculation after sale deletion
            try {
                log.info("Calling financeSummaryService.autoCreateFinanceSummary after sale deletion for orgId={}", orgId);
                financeSummaryService.autoCreateFinanceSummary(orgId);
                log.info("FinanceSummary auto-updated after sale deletion for orgId={}", orgId);
            } catch (Exception fsEx) {
                log.error("FinanceSummary auto-update failed after sale delete for orgId={}: {}", orgId, fsEx.getMessage(), fsEx);
            }

        } catch (Exception e) {
            log.error("Error deleting sale id={}: {}", saleId, e.getMessage(), e);
            throw new RuntimeException("Error deleting sale: " + e.getMessage());
        }
    }

    private SalesResponseDTO toResponse(Sales sale) {
        LocalDateTime utcTime = sale.getDateTime();
        LocalDateTime istTime;
        if (utcTime != null) {
            istTime = utcTime.atZone(ZoneId.of("UTC"))
                    .withZoneSameInstant(ZoneId.of("Asia/Kolkata"))
                    .toLocalDateTime();
        } else {
            istTime = null;
        }
        // Convert UTC to IST with zone info
        ZonedDateTime istZoned = utcTime.atZone(ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of("Asia/Kolkata"));
        String displayTime = istZoned.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        return SalesResponseDTO.builder()
                .id(sale.getId())
                .organizationId(sale.getOrganizationId())
                .dateTime(istTime) // ALWAYS IST
                .productName(sale.getProductName())
                .guns(sale.getGuns())
                .empId(sale.getEmpId())
                .openingStock(sale.getOpeningStock())
                .closingStock(sale.getClosingStock())
                .testingTotal(sale.getTestingTotal())
                .salesInLiters(sale.getSalesInLiters())
                .price(sale.getPrice())
                .salesInRupees(sale.getSalesInRupees())
                .testingTotal(sale.getTestingTotal())
                .build();
    }

}
