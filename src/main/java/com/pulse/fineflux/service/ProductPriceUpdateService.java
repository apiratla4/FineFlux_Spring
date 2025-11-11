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
// If you have spring-tx on classpath, you may annotate @Transactional for atomicity

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.pulse.fineflux.utill.DateTimeUtil;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductPriceUpdateService {

    private final ProductRepository productRepo;
    private final InventoryRepository inventoryRepo;
    private final InventoryLogRepository logRepo;

    public void updateProductPrice(String productId, double newPrice, String empId) {
        try {
            // 1) Update Product price (+ compute and update product stockValue)
            Product product = productRepo.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

            product.setPrice(newPrice);
            product.setLastUpdated(DateTimeUtil.nowLocal());

            // NEW: compute product-level stockValue = product.currentLevel * newPrice
            BigDecimal prodCurrentLevel = defaultZero(product.getCurrentLevel());
            BigDecimal productStockValue = prodCurrentLevel.multiply(BigDecimal.valueOf(newPrice));

            // If your Product entity already has a stockValue field, set it here.
            // If not present, remove the next line or add the field in your entity.

            productRepo.save(product);

            // 2) Update all inventories for this product (stockValue = currentLevel * newPrice)
            List<Inventory> inventories = inventoryRepo.findByProductId(productId);
            if (inventories == null || inventories.isEmpty()) {
                throw new RuntimeException("No inventory found for productId: " + productId);
            }

            for (Inventory inventory : inventories) {
                BigDecimal invCurrentLevel = defaultZero(inventory.getCurrentLevel());
                BigDecimal invStockValue = invCurrentLevel.multiply(BigDecimal.valueOf(newPrice));

                inventory.setStockValue(invStockValue);
                inventory.setLastUpdated(DateTimeUtil.nowLocal());
                inventoryRepo.save(inventory);

                // 3) Log the change
                InventoryLog invLog = InventoryLog.builder()
                        .inventoryId(inventory.getInventoryId())
                        .organizationId(inventory.getOrganizationId())
                        .productId(productId)
                        .productName(product.getProductName())
                        .totalCapacity(inventory.getTotalCapacity())
                        .stockValue(invStockValue)
                        .currentLevel(inventory.getCurrentLevel())
                        .metric(inventory.getMetric())
                        .tankCapacity(inventory.getTankCapacity())
                        .status(inventory.getStatus())
                        .lastUpdated(DateTimeUtil.nowLocal())
                        .empId(empId)
                        .build();
                logRepo.save(invLog);
            }

            log.info("Product price updated (and product stockValue computed) across {} inventory records: productId={}, newPrice={}, empId={}",
                    inventories.size(), productId, newPrice, empId);

        } catch (Exception e) {
            log.error("Failed to update product price for productId={}, empId={}. Error: {}", productId, empId, e.getMessage(), e);
            throw new RuntimeException("Failed to update product and inventory price", e);
        }
    }

    private static BigDecimal defaultZero(BigDecimal v) {
        return Objects.requireNonNullElse(v, BigDecimal.ZERO);
    }
}
