package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.InventoryCreateDTO;
import com.pulse.fineflux.domain.InventoryResponseDTO;
import com.pulse.fineflux.domain.InventoryUpdateDTO;
import com.pulse.fineflux.entity.Expense;
import com.pulse.fineflux.entity.Inventory;
import com.pulse.fineflux.entity.InventoryLog;
import com.pulse.fineflux.entity.Product;
import com.pulse.fineflux.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryLogRepository inventoryLogRepository;
    private final ProductRepository productRepository;
    private final ProfitLossService profitLossService;
    // At the top of your InventoryServiceImpl, with your others:
    private final ExpenseRepository expenseRepository;
    private final ExpenseCategoryRepository expenseCategoryRepository;


    // CREATE (used first time only)
    @Override
    @Transactional
    public InventoryResponseDTO createInventory(InventoryCreateDTO dto) {
        try {
            log.info("Creating inventory for productId={} orgId={}", dto.getProductId(), dto.getOrganizationId());
            Product product = productRepository.findByIdAndOrganizationId(dto.getProductId(), dto.getOrganizationId())
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + dto.getProductId()));

            if (!Boolean.TRUE.equals(product.getStatus())) {
                throw new IllegalStateException("Cannot create inventory for INACTIVE product: " + product.getProductName());
            }

            BigDecimal previousLevel = inventoryLogRepository
                    .findTopByProductIdOrderByLastUpdatedDesc(dto.getProductId())
                    .map(InventoryLog::getCurrentLevel)
                    .orElse(BigDecimal.ZERO);

            BigDecimal newCurrentLevel = previousLevel.add(dto.getCurrentLevel() != null ? dto.getCurrentLevel() : BigDecimal.ZERO);

            if (product.getTankCapacity() != null && newCurrentLevel.compareTo(product.getTankCapacity()) > 0) {
                throw new IllegalStateException(String.format(
                        "Adding %.2f exceeds tank capacity %.2f for product '%s'",
                        dto.getCurrentLevel(), product.getTankCapacity(), product.getProductName()
                ));
            }

            BigDecimal totalCapacity = productRepository.findByOrganizationId(dto.getOrganizationId()).stream()
                    .map(p -> p.getTankCapacity() != null ? p.getTankCapacity() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal price = product.getPrice() != null ? BigDecimal.valueOf(product.getPrice()) : BigDecimal.ZERO;
            BigDecimal stockValue = price.multiply(newCurrentLevel);

            Inventory inventory = Inventory.builder()
                    .organizationId(dto.getOrganizationId())
                    .productId(dto.getProductId())
                    .productName(product.getProductName())
                    .totalCapacity(totalCapacity)
                    .stockValue(stockValue)
                    .lastUpdated(LocalDateTime.now(ZoneId.of("Asia/Kolkata")))
                    .empId(dto.getEmpId())
                    .currentLevel(newCurrentLevel)
                    .metric(dto.getMetric())
                    .status(true)
                    .tankCapacity(product.getTankCapacity())
                    .build();

            Inventory savedInventory = inventoryRepository.save(inventory);

            product.setCurrentLevel(newCurrentLevel);
            productRepository.save(product);

            InventoryLog logEntry = InventoryLog.builder()
                    .inventoryId(savedInventory.getInventoryId())
                    .organizationId(savedInventory.getOrganizationId())
                    .productId(savedInventory.getProductId())
                    .productName(savedInventory.getProductName())
                    .totalCapacity(savedInventory.getTotalCapacity())
                    .stockValue(savedInventory.getStockValue())
                    .lastUpdated(savedInventory.getLastUpdated())
                    .empId(savedInventory.getEmpId())
                    .currentLevel(savedInventory.getCurrentLevel())
                    .metric(savedInventory.getMetric())
                    .status(savedInventory.getStatus())
                    .tankCapacity(savedInventory.getTankCapacity())
                    .receiptQuantityInLitres(0.0)  // <-- Always zero on initial create
                    .build();
            inventoryLogRepository.save(logEntry);

            profitLossService.calculateAndSaveProfitLoss(dto.getOrganizationId());

            log.debug("Inventory created and log inserted: inventoryId={}, productId={}", savedInventory.getInventoryId(), dto.getProductId());
            return mapToResponse(savedInventory);

        } catch (Exception e) {
            log.error("Error creating inventory productId={} orgId={}", dto.getProductId(), dto.getOrganizationId(), e);
            throw e;
        }
    }

    // ADD/UPDATE via PUT (ALWAYS send only increment! This will update cumulative total)
    @Override
    @Transactional
    public List<InventoryResponseDTO> updateInventory(String orgId, String productId, InventoryUpdateDTO dto) {
        try {
            log.info("Updating inventory for orgId={}, productId={}", orgId, productId);

            Product product = productRepository.findByIdAndOrganizationId(productId, orgId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            BigDecimal increment = dto.getCurrentLevel() != null ? dto.getCurrentLevel() : BigDecimal.ZERO;
            BigDecimal prevTotal = product.getCurrentLevel() != null ? product.getCurrentLevel() : BigDecimal.ZERO;
            BigDecimal newTotal = prevTotal.add(increment);

            if (product.getTankCapacity() != null && newTotal.compareTo(product.getTankCapacity()) > 0) {
                throw new IllegalStateException("Tank overflow! Too much stock");
            }

            BigDecimal price = product.getPrice() != null ? BigDecimal.valueOf(product.getPrice()) : BigDecimal.ZERO;
            BigDecimal computedStockValue = price.multiply(newTotal);

            Inventory inventory = Inventory.builder()
                    .organizationId(orgId)
                    .productId(productId)
                    .productName(product.getProductName())
                    .totalCapacity(dto.getTotalCapacity())
                    .stockValue(computedStockValue)
                    .lastUpdated(LocalDateTime.now(ZoneId.of("Asia/Kolkata")))
                    .empId(dto.getEmpId())
                    .currentLevel(newTotal)
                    .metric(dto.getMetric())
                    .status(dto.getStatus())
                    .tankCapacity(dto.getTankCapacity())
                    .build();

            Inventory savedRecord = inventoryRepository.save(inventory);

            product.setCurrentLevel(newTotal);
            productRepository.save(product);

            InventoryLog historyLog = InventoryLog.builder()
                    .inventoryId(savedRecord.getInventoryId())
                    .organizationId(savedRecord.getOrganizationId())
                    .productId(savedRecord.getProductId())
                    .productName(savedRecord.getProductName())
                    .totalCapacity(savedRecord.getTotalCapacity())
                    .stockValue(computedStockValue)
                    .lastUpdated(savedRecord.getLastUpdated())
                    .empId(savedRecord.getEmpId())
                    .currentLevel(savedRecord.getCurrentLevel())
                    .metric(savedRecord.getMetric())
                    .status(savedRecord.getStatus())
                    .tankCapacity(savedRecord.getTankCapacity())
                    .receiptQuantityInLitres(dto.getCurrentLevel() != null ? dto.getCurrentLevel().doubleValue() : 0.0) // The *increment* (e.g., 100)
                    .build();
            inventoryLogRepository.save(historyLog);

           //profitLossService.calculateAndSaveProfitLoss(orgId);

          /*  // --------- INVENTORY EXPENSES LOGIC INTEGRATION (PER-INCREMENT ONLY) -----------
            try {
                var inventoryCategory = expenseCategoryRepository.findByCategoryNameAndOrganizationId("inventory", orgId);

                if (inventoryCategory.isPresent()) {
                    // Calculate only for the increment (not total). E.g., added 100, price 97.56 => only 9756 logged
                    BigDecimal increment1 = dto.getCurrentLevel() != null ? dto.getCurrentLevel() : BigDecimal.ZERO;
                    BigDecimal productprice = increment1.multiply(price); // use current product price

                    if (increment.compareTo(BigDecimal.ZERO) > 0) { // only positive increments
                        Expense expense = Expense.builder()
                                .description("inventory expenses")
                                .amount(productprice.doubleValue())
                                .categoryName("inventory")
                                .expenseDate(java.time.LocalDate.now())
                                .createdAt(LocalDateTime.now())
                                .organizationId(orgId)
                                .empId(dto.getEmpId())
                                .build();

                        expenseRepository.save(expense);
                        log.info("Auto-inserted inventory expense for orgId={}, productId={}, increment={}, amount={}", orgId, productId, increment, productprice);
                    } else {
                        log.info("Inventory increment is zero or negative ({}), skipping expense insert", increment);
                    }
                } else {
                    log.info("No 'inventory' category found for orgId={}, not inserting inventory expense", orgId);
                }
            } catch(Exception ex) {
                log.error("Error inserting inventory expense for orgId={} productId={}: {}", orgId, productId, ex.getMessage(), ex);
            }
// --------- END INVENTORY EXPENSES LOGIC -----------

           */

            log.debug("Inventory updated: total={}, stockValue={}, inventoryId={}", newTotal, computedStockValue, savedRecord.getInventoryId());

            return inventoryRepository.findAllByOrganizationId(orgId).stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error updating inventory productId={} orgId={}", productId, orgId, e);
            throw e;
        }
    }


    @Override
    public List<InventoryResponseDTO> getAllInventories(String orgId) {
        try {
            log.info("Fetching all inventories for orgId={}", orgId);
            List<InventoryResponseDTO> inventories = inventoryRepository.findAllByOrganizationId(orgId).stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
            log.debug("Fetched {} inventories for orgId={}", inventories.size(), orgId);
            return inventories;
        } catch (Exception e) {
            log.error("Error fetching inventories for orgId={}", orgId, e);
            throw e;
        }
    }

    @Override
    public InventoryResponseDTO getLatestInventory(String orgId, String productId) {
        Inventory latest = inventoryRepository.findTopByOrganizationIdAndProductIdOrderByLastUpdatedDesc(orgId, productId);
        if (latest == null) {
            throw new RuntimeException("No inventory found for org=" + orgId + " product=" + productId);
        }
        return mapToResponse(latest);
    }

    @Override
    @Transactional
    public void deleteInventory(String orgId, String inventoryId) {
        try {
            log.info("Deleting inventory id={} orgId={}", inventoryId, orgId);
            inventoryRepository.deleteById(inventoryId);
            log.debug("Inventory deleted successfully inventoryId={}", inventoryId);
        } catch (Exception e) {
            log.error("Error deleting inventory inventoryId={} orgId={}", inventoryId, orgId, e);
            throw e;
        }
    }

    @Override
    public List<Inventory> getInventoriesByProductAndOrg(String orgId, String productId) {
        return inventoryRepository.findAllByOrganizationIdAndProductId(orgId, productId);
    }

    @Override
    public void saveInventory(Inventory inventory) {
        inventoryRepository.save(inventory);
    }

    @Override
    public InventoryLog getInventoryLogByInventoryId(String inventoryId) {
        return inventoryLogRepository.findByInventoryId(inventoryId);
    }

    private InventoryResponseDTO mapToResponse(Inventory entity) {
        return InventoryResponseDTO.builder()
                .inventoryId(entity.getInventoryId())
                .organizationId(entity.getOrganizationId())
                .productId(entity.getProductId())
                .productName(entity.getProductName())
                .totalCapacity(entity.getTotalCapacity())
                .stockValue(entity.getStockValue())
                .lastUpdated(entity.getLastUpdated())
                .empId(entity.getEmpId())
                .currentLevel(entity.getCurrentLevel())
                .metric(entity.getMetric())
                .status(entity.getStatus())
                .tankCapacity(entity.getTankCapacity())
                .build();
    }
}
