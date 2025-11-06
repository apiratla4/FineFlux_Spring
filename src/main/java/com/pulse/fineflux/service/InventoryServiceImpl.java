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
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
    private final FinanceSummaryService financeSummaryService;


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

            // ---- STORE IST WITH OFFSET (ZonedDateTime for correct serialization) ----
            ZonedDateTime istNow = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));

            Inventory inventory = Inventory.builder()
                    .organizationId(dto.getOrganizationId())
                    .productId(dto.getProductId())
                    .productName(product.getProductName())
                    .totalCapacity(totalCapacity)
                    .stockValue(stockValue)
                    .lastUpdated(istNow.toLocalDateTime()) // store as ZonedDateTime!
                    .empId(dto.getEmpId())
                    .currentLevel(newCurrentLevel)
                    .metric(dto.getMetric())
                    .status(true)
                    .tankCapacity(product.getTankCapacity())
                    .build();

            Inventory savedInventory = inventoryRepository.save(inventory);
            product.setCurrentLevel(newCurrentLevel);
            productRepository.save(product);

            LocalDateTime utcDeleteTime1 = LocalDateTime.now(ZoneId.of("Asia/Kolkata"))
                    .atZone(ZoneId.of("Asia/Kolkata"))
                    .withZoneSameInstant(ZoneId.of("UTC"))
                    .toLocalDateTime();
            InventoryLog logEntry = InventoryLog.builder()
                    .inventoryId(savedInventory.getInventoryId())
                    .organizationId(savedInventory.getOrganizationId())
                    .productId(savedInventory.getProductId())
                    .productName(savedInventory.getProductName())
                    .totalCapacity(savedInventory.getTotalCapacity())
                    .stockValue(savedInventory.getStockValue())
                    .lastUpdated(utcDeleteTime1)
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
    public List<InventoryResponseDTO> updateInventory(String orgId, String productId, String empId, InventoryUpdateDTO dto) {
        try {
            log.info("Updating inventory for orgId={}, productId={} by empId={}", orgId, productId, empId);

            Product product = productRepository.findByIdAndOrganizationId(productId, orgId)
                    .orElseThrow(() -> new RuntimeException("Product not found")); // repository find [web:14]

            BigDecimal increment = dto.getCurrentLevel() != null ? dto.getCurrentLevel() : BigDecimal.ZERO;
            BigDecimal prevTotal = product.getCurrentLevel() != null ? product.getCurrentLevel() : BigDecimal.ZERO;
            BigDecimal newTotal = prevTotal.add(increment);

            if (product.getTankCapacity() != null && newTotal.compareTo(product.getTankCapacity()) > 0) {
                throw new IllegalStateException("Tank overflow! Too much stock"); // validation [web:110]
            }

            BigDecimal price = product.getPrice() != null ? BigDecimal.valueOf(product.getPrice()) : BigDecimal.ZERO;
            BigDecimal computedStockValue = price.multiply(newTotal);

            LocalDateTime istTime = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
            // Persist latest inventory snapshot
            Inventory inventory = Inventory.builder()
                    .organizationId(orgId)
                    .productId(productId)
                    .productName(product.getProductName())
                    .totalCapacity(dto.getTotalCapacity())
                    .stockValue(computedStockValue)
                    .lastUpdated(istTime)
                    .empId(empId) // FIX: set current operator here
                    .currentLevel(newTotal)
                    .metric(dto.getMetric())
                    .status(dto.getStatus())
                    .tankCapacity(dto.getTankCapacity())
                    .build();

            Inventory savedRecord = inventoryRepository.save(inventory); // repository save [web:14]

            // Update product current level
            product.setCurrentLevel(newTotal);
            productRepository.save(product); // repository save [web:14]

            // Write inventory history log (immutable trail)
            InventoryLog historyLog = InventoryLog.builder()
                    .inventoryId(savedRecord.getInventoryId())
                    .organizationId(savedRecord.getOrganizationId())
                    .productId(savedRecord.getProductId())
                    .productName(savedRecord.getProductName())
                    .totalCapacity(savedRecord.getTotalCapacity())
                    .stockValue(computedStockValue)
                    .lastUpdated(savedRecord.getLastUpdated())
                    .empId(empId) // FIX: log the same operator who updated
                    .currentLevel(savedRecord.getCurrentLevel())
                    .metric(savedRecord.getMetric())
                    .status(savedRecord.getStatus())
                    .tankCapacity(savedRecord.getTankCapacity())
                    .receiptQuantityInLitres(increment.doubleValue()) // the increment only
                    .mutationby("inventory Stock updated by " + empId) // FIX: based on current operator
                    .build();
            inventoryLogRepository.save(historyLog);
            financeSummaryService.autoCreateFinanceSummary(savedRecord.getOrganizationId());

            // Build and return response list as per your implementation
            return inventoryRepository.findAllByOrganizationIdAndProductId(orgId, productId)
                    .stream()
                    .map(this::mapToResponse) // FIX: use the existing mapper
                    .toList(); // if on Java <16, use .collect(Collectors.toList())


        } catch (Exception e) {
            log.error("Error updating inventory for orgId={}, productId={}, empId={}: {}", orgId, productId, empId, e.getMessage(), e);
            throw new RuntimeException("Error updating inventory: " + e.getMessage()); // consistent error path [web:110]
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
    public void deleteInventory(String orgId, String inventoryId, String employeeId) {
        try {
            log.info("Deleting inventory id={} orgId={}", inventoryId, orgId);

            // 1. Get inventory BEFORE deleting, so you can use all its details
            Inventory inv = inventoryRepository.findById(inventoryId)
                    .orElseThrow(() -> new RuntimeException("Inventory not found for deletion"));

            String productId = inv.getProductId();

            // 2. Get product BEFORE deleting inventory (prevents not found errors!)
            Product product = productRepository.findByIdAndOrganizationId(productId, orgId)
                    .orElseThrow(() -> new RuntimeException(
                            "Product not found for inventory delete (possible data corruption or orphan inventory)"
                    ));

            // 3. Find previous InventoryLog for this product, excluding the soon-to-be-deleted inventoryId
            InventoryLog prevLog = inventoryLogRepository.findTopByProductIdAndInventoryIdNotOrderByLastUpdatedDesc(
                    productId, inventoryId
            );
            BigDecimal previousLevel = (prevLog != null) ? prevLog.getCurrentLevel() : BigDecimal.ZERO;
            BigDecimal previousStockValue = (prevLog != null) ? prevLog.getStockValue() : BigDecimal.ZERO;
            // 4. Restore product currentLevel to previous most recent value
            product.setCurrentLevel(previousLevel);

            productRepository.save(product);

            // 5. Log mutation in InventoryLog
            InventoryLog deleteLog = InventoryLog.builder()
                    .inventoryId(inv.getInventoryId())
                    .organizationId(inv.getOrganizationId())
                    .productId(inv.getProductId())
                    .productName(inv.getProductName())
                    .totalCapacity(inv.getTotalCapacity())
                    .stockValue(previousStockValue)
                    .lastUpdated(LocalDateTime.now(ZoneId.of("Asia/Kolkata")))
                    .empId(employeeId)
                    .currentLevel(previousLevel) // The "restored" value
                    .metric(inv.getMetric())
                    .status(inv.getStatus())
                    .tankCapacity(inv.getTankCapacity())
                    .mutationby("inventory deleted by " + employeeId)
                    .build();

            inventoryLogRepository.save(deleteLog);

            // 6. Now delete the inventory record
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
