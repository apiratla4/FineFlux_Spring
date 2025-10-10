package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.*;
import com.pulse.fineflux.entity.*;
import com.pulse.fineflux.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalesServiceImpl implements SalesService {

    private final SalesRepository salesRepository;
    private final GunInfoRepository gunInfoRepository;
    private final ProductRepository productRepository;
    private final ProfitLossService profitLossService;
    private final InventoryRepository inventoryRepository;
    private final InventoryLogRepository inventoryLogRepository;

    @Override
    public SalesResponseDTO createSale(SalesCreateDTO dto) {
        try {
            LocalDateTime entryDateTime = dto.getDateTime() != null ? dto.getDateTime() : LocalDateTime.now();

            // Lookup product by name and org
            Product product = productRepository.findByOrganizationId(dto.getOrganizationId()).stream()
                    .filter(p -> p.getProductName().trim().equalsIgnoreCase(dto.getProductName().trim()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Product not found: " + dto.getProductName()));

            // Lookup gun info (optional only for generic sales)
            String gunName = gunInfoRepository.findByOrganizationId(dto.getOrganizationId())
                    .stream().findFirst().map(GunInfo::getGuns).orElse("N/A");

            // Calculate true sale volume
            double opening = dto.getOpeningStock() == 0f
                    ? getLastClosing(product.getProductName(), gunName)
                    : dto.getOpeningStock();
            double closing = dto.getClosingStock();
            double testing = dto.getTestingTotal();

            BigDecimal saleVolume = BigDecimal.valueOf(closing - opening - testing);
            float amount = saleVolume.multiply(BigDecimal.valueOf(dto.getPrice())).floatValue();

            // Save sale record
            Sales sale = Sales.builder()
                    .organizationId(dto.getOrganizationId())
                    .dateTime(entryDateTime)
                    .productName(product.getProductName())
                    .guns(gunName)
                    .empId(dto.getEmpId())
                    .openingStock(opening)
                    .closingStock(closing)
                    .testingTotal(testing)
                    .salesInLiters(saleVolume.floatValue())
                    .price(dto.getPrice())
                    .salesInRupees(amount)
                    .build();

            Sales saved = salesRepository.save(sale);

            // Profit/loss calculation triggered for org
            profitLossService.calculateAndSaveProfitLoss(dto.getOrganizationId());
            log.info("Sale created successfully: {}", saved);

            // 1. PRODUCT: Decrease stock
            BigDecimal currProduct = product.getCurrentLevel() != null ? product.getCurrentLevel() : BigDecimal.ZERO;
            BigDecimal decreaseBy = BigDecimal.valueOf(saved.getSalesInLiters());
            BigDecimal updatedProductLevel = currProduct.subtract(decreaseBy);
            if (updatedProductLevel.compareTo(BigDecimal.ZERO) < 0) updatedProductLevel = BigDecimal.ZERO;
            product.setCurrentLevel(updatedProductLevel);
            productRepository.save(product);
            log.info("Product '{}' currentLevel updated: {} → {} (decreased by {})",
                    product.getProductName(), currProduct, updatedProductLevel, decreaseBy);

            // 2. INVENTORY and 3. HISTORY LOG
            inventoryRepository.findByOrganizationIdAndProductId(dto.getOrganizationId(), product.getId())
                    .ifPresent(inventory -> {
                        BigDecimal currInv = inventory.getCurrentLevel() != null ? inventory.getCurrentLevel() : BigDecimal.ZERO;
                        BigDecimal updatedInv = currInv.subtract(decreaseBy);
                        if (updatedInv.compareTo(BigDecimal.ZERO) < 0) updatedInv = BigDecimal.ZERO;

                        inventory.setCurrentLevel(updatedInv);
                        inventoryRepository.save(inventory);

                        // Insert InventoryLog as history
                        InventoryLog logEntry = InventoryLog.builder()
                                .inventoryId(inventory.getInventoryId())
                                .organizationId(inventory.getOrganizationId())
                                .productId(inventory.getProductId())
                                .productName(inventory.getProductName())
                                .totalCapacity(inventory.getTotalCapacity())
                                .stockValue(inventory.getStockValue())
                                .lastUpdated(java.sql.Timestamp.valueOf(entryDateTime))
                                .empId(dto.getEmpId())
                                .currentLevel(updatedInv)
                                .metric(inventory.getMetric())
                                .status(inventory.getStatus())
                                .tankCapacity(inventory.getTankCapacity())
                                .build();

                        inventoryLogRepository.save(logEntry);

                        log.info("Inventory '{}' currentLevel updated: {} → {} (decreased by {})",
                                inventory.getProductName(), currInv, updatedInv, decreaseBy);
                    });

            return toResponse(saved);

        } catch (Exception e) {
            log.error("❌ Error creating sale for orgId={} productName={}: {}", dto.getOrganizationId(), dto.getProductName(), e.getMessage(), e);
            throw new RuntimeException("Error creating sale: " + e.getMessage());
        }
    }

    // Last closing stock calculation (unchanged)
    public double getLastClosing(String productName, String gun) {
        try {
            log.info("Fetching last closing for product {} and gun {}", productName, gun);
            Sales last = salesRepository.findTopByProductNameAndGunsOrderByDateTimeDesc(productName, gun);
            double closing = (last != null) ? last.getClosingStock() : 0f;
            log.info("Last closing found: {}", closing);
            return closing;
        } catch (Exception e) {
            log.error("Error fetching last closing: {}", e.getMessage(), e);
            return 0f;
        }
    }

    @Override
    public SalesResponseDTO updateSale(String id, SalesUpdateDTO dto) {
        try {
            Sales sale = salesRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Sale not found"));

            sale.setOpeningStock(dto.getOpeningStock());
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
    public void deleteSale(String id) {
        try {
            log.info("Deleting sale id={}", id);
            salesRepository.deleteById(id);
        } catch (Exception e) {
            log.error("Error deleting sale id={}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Error deleting sale: " + e.getMessage());
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

    private SalesResponseDTO toResponse(Sales sale) {
        return SalesResponseDTO.builder()
                .id(sale.getId())
                .organizationId(sale.getOrganizationId())
                .dateTime(sale.getDateTime())
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
