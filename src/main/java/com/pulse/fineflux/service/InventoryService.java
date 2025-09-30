package com.pulse.fineflux.service;

import com.pulse.fineflux.entity.Inventory;
import com.pulse.fineflux.entity.InventoryLog;
import com.pulse.fineflux.entity.Product;
import com.pulse.fineflux.repository.InventoryLogRepository;
import com.pulse.fineflux.repository.InventoryRepository;
import com.pulse.fineflux.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class InventoryService {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryLogRepository inventoryLogRepository;

    public InventoryService(
            InventoryRepository inventoryRepository,
            ProductRepository productRepository,
            InventoryLogRepository inventoryLogRepository
    ) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.inventoryLogRepository = inventoryLogRepository;
    }

    /**
     * Create a new inventory record.
     */
    @Transactional
    public Inventory createInventory(Inventory inventory) {
        try {
            if (inventory.getLastUpdated() == null) {
                inventory.setLastUpdated(new Date());
            }

            // Save inventory
            Inventory savedInventory = inventoryRepository.save(inventory);

            // Update product's current level
            Product product = productRepository.findById(inventory.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + inventory.getProductId()));
            BigDecimal previousLevel = product.getCurrentLevel() != null ? product.getCurrentLevel() : BigDecimal.ZERO;
            BigDecimal newLevel = inventory.getCurrentStock() != null ? inventory.getCurrentStock() : BigDecimal.ZERO;

            product.setCurrentLevel(newLevel);
            productRepository.save(product);

            // Save inventory log
            InventoryLog logEntry = new InventoryLog();
            logEntry.setProductId(inventory.getProductId());
            logEntry.setQuantity(inventory.getCurrentStock());
            logEntry.setPreviousLevel(previousLevel);
            logEntry.setNewLevel(newLevel);
            logEntry.setCurrentLevel(newLevel);
            logEntry.setMetric(inventory.getMetric());
            logEntry.setEmployeeId(inventory.getEmployeeId());
            logEntry.setTransactionDate(new Date());
            logEntry.setAction("CREATE");
            inventoryLogRepository.save(logEntry);

            log.info("Inventory created. Prev: {}, New: {}. Log recorded for productId: {}",
                    previousLevel, newLevel, inventory.getProductId());

            return savedInventory;

        } catch (Exception e) {
            log.error("Error creating inventory: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Fetch all inventory records.
     */
    public List<Inventory> getAllInventories() {
        try {
            List<Inventory> inventories = inventoryRepository.findAll();
            log.info("Fetched {} inventories", inventories.size());
            return inventories;
        } catch (Exception e) {
            log.error("Error fetching inventories: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Update inventories and log changes by productId.
     */
    @Transactional
    public List<Inventory> updateInventoriesByProductId(String productId, Inventory updatedInventory) {
        try {
            List<Inventory> inventories = inventoryRepository.findAllByProductId(productId);

            if (inventories.isEmpty()) {
                throw new RuntimeException("No inventories found for productId: " + productId);
            }

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

            BigDecimal previousLevel = product.getCurrentLevel() != null ? product.getCurrentLevel() : BigDecimal.ZERO;
            BigDecimal newLevel = updatedInventory.getCurrentStock() != null ? updatedInventory.getCurrentStock() : BigDecimal.ZERO;

            // Update inventories
            for (Inventory existingInventory : inventories) {
                existingInventory.setCurrentStock(updatedInventory.getCurrentStock());
                existingInventory.setTotalCapacity(updatedInventory.getTotalCapacity());
                existingInventory.setStockValue(updatedInventory.getStockValue());
                existingInventory.setLastUpdated(new Date());
                existingInventory.setEmployeeId(updatedInventory.getEmployeeId());
                existingInventory.setMetric(updatedInventory.getMetric());
                existingInventory.setStatus(updatedInventory.getStatus());
                existingInventory.setTankCapacity(updatedInventory.getTankCapacity());

                inventoryRepository.save(existingInventory);
            }

            // Update product
            product.setCurrentLevel(newLevel);
            productRepository.save(product);

            // Save log entry
            InventoryLog logEntry = new InventoryLog();
            logEntry.setProductId(productId);
            logEntry.setQuantity(updatedInventory.getCurrentStock());
            logEntry.setPreviousLevel(previousLevel);
            logEntry.setNewLevel(newLevel);
            logEntry.setCurrentLevel(newLevel);
            logEntry.setMetric(updatedInventory.getMetric());
            logEntry.setEmployeeId(updatedInventory.getEmployeeId());
            logEntry.setTransactionDate(new Date());
            logEntry.setAction("UPDATE");
            inventoryLogRepository.save(logEntry);

            log.info("Inventory updated. Prev: {}, New: {}. Log recorded for productId: {}",
                    previousLevel, newLevel, productId);

            return inventories;

        } catch (Exception e) {
            log.error("Error updating inventory: {}", e.getMessage(), e);
            throw e;
        }
    }
}
