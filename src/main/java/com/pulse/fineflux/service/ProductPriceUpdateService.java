package com.pulse.fineflux.service;

import com.pulse.fineflux.entity.Product;
import com.pulse.fineflux.entity.Inventory;
import com.pulse.fineflux.entity.InventoryLog;
import com.pulse.fineflux.repository.ProductRepository;
import com.pulse.fineflux.repository.InventoryRepository;
import com.pulse.fineflux.repository.InventoryLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductPriceUpdateService {

    private final ProductRepository productRepo;
    private final InventoryRepository inventoryRepo;
    private final InventoryLogRepository logRepo;

    /**
     * Updates Product price, cascades changes to Inventory and InventoryLog.
     * @param productId The product identifier.
     * @param newPrice  The new price to set.
     * @param empId     The employee performing the update.
     */
    public void updateProductPrice(String productId, double newPrice, String empId) {
        try {
            // 1. Update Product price
            Product product = productRepo.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
            product.setPrice(newPrice);
            product.setLastUpdated(LocalDateTime.now());
            productRepo.save(product);

            // 2. Update Inventory stockValue (stockValue = currentLevel * newPrice)
            Inventory inventory = inventoryRepo.findByProductId(productId);
            if (inventory == null) throw new RuntimeException("Inventory not found for productId: " + productId);

            BigDecimal stockValue = inventory.getCurrentLevel().multiply(BigDecimal.valueOf(newPrice));
            inventory.setStockValue(stockValue);
            inventory.setLastUpdated(LocalDateTime.now());
            inventoryRepo.save(inventory);

            // 3. Log the price & stock value change in InventoryLog
            InventoryLog invLog = InventoryLog.builder()
                    .inventoryId(inventory.getInventoryId())
                    .organizationId(inventory.getOrganizationId())
                    .productId(productId)
                    .productName(product.getProductName())
                    .totalCapacity(inventory.getTotalCapacity())
                    .stockValue(stockValue)
                    .currentLevel(inventory.getCurrentLevel())
                    .metric(inventory.getMetric())
                    .tankCapacity(inventory.getTankCapacity())
                    .status(inventory.getStatus())
                    .lastUpdated(LocalDateTime.now())
                    .empId(empId)
                    .build();
            logRepo.save(invLog);

            log.info("Product price updated: productId={}, newPrice={}, empId={}", productId, newPrice, empId);
        } catch (Exception e) {
            log.error("Failed to update product price for productId={}, empId={}. Error: {}", productId, empId, e.getMessage(), e);
            throw new RuntimeException("Failed to update product and inventory price", e);
        }
    }
}
