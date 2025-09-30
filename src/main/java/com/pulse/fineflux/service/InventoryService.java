package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.InventoryCreateDTO;
import com.pulse.fineflux.domain.InventoryResponseDTO;
import com.pulse.fineflux.domain.InventoryUpdateDTO;
import com.pulse.fineflux.entity.Inventory;
import com.pulse.fineflux.entity.InventoryLog;
import com.pulse.fineflux.entity.Product;
import com.pulse.fineflux.repository.InventoryLogRepository;
import com.pulse.fineflux.repository.InventoryRepository;
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
@Slf4j
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final InventoryLogRepository inventoryLogRepository;

    /**
     * Create a new inventory record
     * Updates the product's current level and creates an inventory log
     */
    @Transactional
    public InventoryResponseDTO createInventory(InventoryCreateDTO dto) {
        log.info("Starting inventory creation for productId={}", dto.getProductId());
        try {
            // Build and save inventory entity
            Inventory inventory = Inventory.builder()
                    .currentStock(dto.getCurrentStock())
                    .totalCapacity(dto.getTotalCapacity())
                    .stockValue(dto.getStockValue())
                    .lastUpdated(new Date())
                    .employeeId(dto.getEmployeeId())
                    .metric(dto.getMetric())
                    .productId(dto.getProductId())
                    .status(dto.getStatus())
                    .tankCapacity(dto.getTankCapacity())
                    .build();

            Inventory savedInventory = inventoryRepository.save(inventory);
            log.info("Inventory saved: inventoryId={}", savedInventory.getInventoryId());

            // Update product's current level
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + dto.getProductId()));
            BigDecimal previousLevel = product.getCurrentLevel() != null ? product.getCurrentLevel() : BigDecimal.ZERO;
            product.setCurrentLevel(dto.getCurrentStock());
            productRepository.save(product);
            log.info("Product updated: productId={}, previousLevel={}, newLevel={}",
                    product.getProductId(), previousLevel, dto.getCurrentStock());

            // Save inventory log
            InventoryLog logEntry = InventoryLog.builder()
                    .productId(dto.getProductId())
                    .quantity(dto.getCurrentStock())
                    .previousLevel(previousLevel)
                    .newLevel(dto.getCurrentStock())
                    .currentLevel(dto.getCurrentStock())
                    .metric(dto.getMetric())
                    .employeeId(dto.getEmployeeId())
                    .transactionDate(new Date())
                    .productName(dto.getProductName())
                    .action("CREATE")
                    .build();
            inventoryLogRepository.save(logEntry);
            log.info("Inventory log saved for productId={}", dto.getProductId());

            return mapToResponseDTO(savedInventory);

        } catch (Exception e) {
            log.error("Failed to create inventory for productId={}: {}", dto.getProductId(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Update existing inventory for a product
     * Updates product level and logs the inventory update
     */
    @Transactional
    public List<InventoryResponseDTO> updateInventory(String productId, InventoryUpdateDTO dto) {
        log.info("Starting inventory update for productId={}", productId);
        try {
            List<Inventory> inventories = inventoryRepository.findAllByProductId(productId);

            if (inventories.isEmpty()) {
                log.warn("No inventories found for productId={}", productId);
                throw new RuntimeException("No inventories found for productId: " + productId);
            }

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

            BigDecimal previousLevel = product.getCurrentLevel() != null ? product.getCurrentLevel() : BigDecimal.ZERO;

            // Update all inventory records for this product
            List<InventoryResponseDTO> response = inventories.stream().map(inv -> {
                inv.setCurrentStock(dto.getCurrentStock());
                inv.setTotalCapacity(dto.getTotalCapacity());
                inv.setStockValue(dto.getStockValue());
                inv.setLastUpdated(new Date());
                inv.setEmployeeId(dto.getEmployeeId());
                inv.setMetric(dto.getMetric());
                inv.setStatus(dto.getStatus());
                inv.setTankCapacity(dto.getTankCapacity());
                inventoryRepository.save(inv);
                return mapToResponseDTO(inv);
            }).collect(Collectors.toList());

            // Update product level
            product.setCurrentLevel(dto.getCurrentStock());
            productRepository.save(product);
            log.info("Product level updated: productId={}, previousLevel={}, newLevel={}",
                    productId, previousLevel, dto.getCurrentStock());

            // Save inventory log
            InventoryLog logEntry = InventoryLog.builder()
                    .productId(productId)
                    .quantity(dto.getCurrentStock())
                    .previousLevel(previousLevel)
                    .newLevel(dto.getCurrentStock())
                    .currentLevel(dto.getCurrentStock())
                    .metric(dto.getMetric())
                    .employeeId(dto.getEmployeeId())
                    .transactionDate(new Date())
                    .action("UPDATE")
                    .build();
            inventoryLogRepository.save(logEntry);
            log.info("Inventory log saved for productId={}", productId);

            return response;

        } catch (Exception e) {
            log.error("Failed to update inventory for productId={}: {}", productId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Fetch all inventory records
     */
    public List<InventoryResponseDTO> getAllInventories() {
        log.info("Fetching all inventories");
        try {
            return inventoryRepository.findAll().stream()
                    .map(this::mapToResponseDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to fetch all inventories: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Delete inventory record by ID
     */
    @Transactional
    public void deleteInventory(String inventoryId) {
        log.info("Starting inventory deletion for inventoryId={}", inventoryId);
        try {
            Inventory inventory = inventoryRepository.findById(inventoryId)
                    .orElseThrow(() -> new RuntimeException("Inventory not found: " + inventoryId));

            inventoryRepository.deleteById(inventoryId);
            log.info("Inventory deleted successfully: inventoryId={}", inventoryId);

        } catch (Exception e) {
            log.error("Failed to delete inventory: inventoryId={}, error={}", inventoryId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Map Inventory entity to InventoryResponseDTO
     */
    private InventoryResponseDTO mapToResponseDTO(Inventory inventory) {
        return InventoryResponseDTO.builder()
                .inventoryId(inventory.getInventoryId())
                .currentStock(inventory.getCurrentStock())
                .totalCapacity(inventory.getTotalCapacity())
                .stockValue(inventory.getStockValue())
                .lastUpdated(inventory.getLastUpdated())
                .employeeId(inventory.getEmployeeId())
                .metric(inventory.getMetric())
                .productId(inventory.getProductId())
                .productName(inventory.getProductName())
                .status(inventory.getStatus())
                .tankCapacity(inventory.getTankCapacity())
                .build();
    }
}
