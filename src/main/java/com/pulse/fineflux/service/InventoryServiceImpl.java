package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.*;
import com.pulse.fineflux.entity.Inventory;
import com.pulse.fineflux.entity.InventoryLog;
import com.pulse.fineflux.repository.InventoryRepository;
import com.pulse.fineflux.repository.InventoryLogRepository;
import com.pulse.fineflux.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    /**
     * Create a new inventory entry with logging and try-catch.
     */
    @Override
    @Transactional
    public InventoryResponseDTO createInventory(InventoryCreateDTO dto) {
        try {
            log.info("Creating inventory for productId={} orgId={}", dto.getProductId(), dto.getOrganizationId());

            // 1️⃣ Fetch product info
            var product = productRepository.findByIdAndOrganizationId(dto.getProductId(), dto.getOrganizationId())
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + dto.getProductId()));

            if (!Boolean.TRUE.equals(product.getStatus())) {
                throw new IllegalStateException("Cannot create inventory for INACTIVE product: " + product.getProductName());
            }

            // 2️⃣ Get latest log to find previous level
            var latestLogOpt = inventoryLogRepository.findTopByProductIdOrderByLastUpdatedDesc(dto.getProductId());
            BigDecimal previousLevel = latestLogOpt.map(InventoryLog::getCurrentLevel).orElse(BigDecimal.ZERO);

            // 3️⃣ Calculate new current level by adding new quantity
            BigDecimal newCurrentLevel = previousLevel.add(dto.getCurrentLevel());

            // 4️⃣ Business Rule: Check if new level exceeds tank capacity
            if (newCurrentLevel.compareTo(product.getTankCapacity()) > 0) {
                throw new IllegalStateException(String.format(
                        "Adding quantity %.2f would exceed tank capacity of %.2f for product '%s'. Current level is %.2f.",
                        dto.getCurrentLevel(), product.getTankCapacity(), product.getProductName(), previousLevel
                ));
            }

            // 5️⃣ Calculate total capacity (sum of all product tank capacities for this org)
            BigDecimal totalCapacity = productRepository.findByOrganizationId(dto.getOrganizationId()).stream()
                    .map(p -> p.getTankCapacity() != null ? p.getTankCapacity() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 6️⃣ Calculate stock value (price × current level)
            BigDecimal stockValue = BigDecimal.valueOf(
                    product.getPrice() != null ? product.getPrice() : 0.0
            ).multiply(newCurrentLevel);

            // 7️⃣ Save inventory
            Inventory inventory = Inventory.builder()
                    .organizationId(dto.getOrganizationId())
                    .productId(dto.getProductId())
                    .productName(product.getProductName())
                    .totalCapacity(totalCapacity)
                    .stockValue(stockValue)
                    .lastUpdated(new Date())
                    .employeeId(dto.getEmployeeId())
                    .currentLevel(newCurrentLevel)
                    .metric(dto.getMetric())
                    .status(true)
                    .tankCapacity(product.getTankCapacity())
                    .build();

            Inventory saved = inventoryRepository.save(inventory);
            profitLossService.calculateAndSaveProfitLoss(dto.getOrganizationId());

            log.debug("✅ Inventory created inventoryId={}", saved.getInventoryId());

            // 8️⃣ Update product level
            product.setCurrentLevel(newCurrentLevel);
            productRepository.save(product);

            // 9️⃣ Save log entry
            inventoryLogRepository.save(InventoryLog.builder()
                    .inventoryId(saved.getInventoryId())
                    .organizationId(saved.getOrganizationId())
                    .productId(saved.getProductId())
                    .productName(saved.getProductName())
                    .totalCapacity(totalCapacity)
                    .stockValue(stockValue)
                    .lastUpdated(saved.getLastUpdated())
                    .employeeId(saved.getEmployeeId())
                    .currentLevel(saved.getCurrentLevel())
                    .metric(saved.getMetric())
                    .status(saved.getStatus())
                    .tankCapacity(saved.getTankCapacity())
                    .build());

            return mapToResponse(saved);
        } catch (Exception e) {
            log.error("❌ Error creating inventory for productId={} orgId={}", dto.getProductId(), dto.getOrganizationId(), e);
            throw e;
        }
    }

    /**
     * Update an existing inventory entry with logging and try-catch.
     */
    @Override
    @Transactional
    public List<InventoryResponseDTO> updateInventory(String orgId, String productId, InventoryUpdateDTO dto) {
        try {
            log.info("Updating inventory productId={} orgId={}", productId, orgId);

            Inventory inventory = inventoryRepository.findByOrganizationIdAndProductId(orgId, productId)
                    .orElseThrow(() -> new RuntimeException("Inventory not found"));

            inventory.setTotalCapacity(dto.getTotalCapacity());
            inventory.setStockValue(dto.getStockValue());
            inventory.setEmployeeId(dto.getEmployeeId());
            inventory.setCurrentLevel(dto.getCurrentLevel());
            inventory.setMetric(dto.getMetric());
            inventory.setStatus(dto.getStatus());
            inventory.setTankCapacity(dto.getTankCapacity());
            inventory.setLastUpdated(new Date());

            Inventory updated = inventoryRepository.save(inventory);

            log.debug("Inventory updated successfully inventoryId={}", updated.getInventoryId());

            // Update product current level automatically
            productRepository.findByIdAndOrganizationId(productId, orgId)
                    .ifPresent(product -> {
                        product.setCurrentLevel(dto.getCurrentLevel());
                        productRepository.save(product);
                        log.debug("Product currentLevel updated productId={}", product.getId());
                    });

            // Create log entry
            InventoryLog logEntry = InventoryLog.builder()
                    .inventoryId(updated.getInventoryId())
                    .organizationId(updated.getOrganizationId())
                    .productId(updated.getProductId())
                    .productName(updated.getProductName())
                    .totalCapacity(updated.getTotalCapacity())
                    .stockValue(updated.getStockValue())
                    .lastUpdated(updated.getLastUpdated())
                    .employeeId(updated.getEmployeeId())
                    .currentLevel(updated.getCurrentLevel())
                    .metric(updated.getMetric())
                    .status(updated.getStatus())
                    .tankCapacity(updated.getTankCapacity())
                    .build();
            inventoryLogRepository.save(logEntry);
            profitLossService.calculateAndSaveProfitLoss(dto.getOrganizationId());
            log.debug("Inventory log saved successfully inventoryId={}", updated.getInventoryId());

            return inventoryRepository.findAllByOrganizationId(orgId).stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error updating inventory productId={} orgId={}", productId, orgId, e);
            throw e;
        }
    }

    /**
     * Get all inventories for an organization with logging and try-catch.
     */
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

    /**
     * Delete an inventory entry with logging and try-catch.
     */
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

    /**
     * Mapper method: Inventory entity -> InventoryResponseDTO
     */
    private InventoryResponseDTO mapToResponse(Inventory entity) {
        return InventoryResponseDTO.builder()
                .inventoryId(entity.getInventoryId())
                .organizationId(entity.getOrganizationId())
                .productId(entity.getProductId())
                .productName(entity.getProductName())
                .totalCapacity(entity.getTotalCapacity())
                .stockValue(entity.getStockValue())
                .lastUpdated(entity.getLastUpdated())
                .employeeId(entity.getEmployeeId())
                .currentLevel(entity.getCurrentLevel())
                .metric(entity.getMetric())
                .status(entity.getStatus())
                .tankCapacity(entity.getTankCapacity())
                .build();
    }
}
