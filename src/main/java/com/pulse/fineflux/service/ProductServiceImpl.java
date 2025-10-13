package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.InventoryCreateDTO;
import com.pulse.fineflux.domain.ProductCreateDTO;
import com.pulse.fineflux.domain.ProductResponseDTO;
import com.pulse.fineflux.domain.ProductUpdateDTO;
import com.pulse.fineflux.entity.Inventory;
import com.pulse.fineflux.entity.InventoryLog;
import com.pulse.fineflux.entity.Product;
import com.pulse.fineflux.repository.InventoryLogRepository;
import com.pulse.fineflux.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final InventoryLogRepository inventoryLogRepository;
    /**
     * Get all products for a specific organization.
     */
    @Override
    public List<ProductResponseDTO> getAllProducts(String orgId) {
        try {
            log.info("Fetching all products for orgId={}", orgId);

            List<ProductResponseDTO> products = productRepository.findByOrganizationId(orgId)
                    .stream()
                    .map(this::toResponse)
                    .toList();

            log.debug("Found {} products for orgId={}", products.size(), orgId);
            return products;
        } catch (Exception e) {
            log.error("Error fetching all products for orgId={}", orgId, e);
            throw new RuntimeException("Failed to fetch products", e);
        }
    }

    /**
     * Get a single product by its ID for a specific organization.
     */
    @Override
    public ProductResponseDTO getProduct(String orgId, String productId) {
        try {
            log.info("Fetching productId={} for orgId={}", productId, orgId);

            Product product = productRepository.findByIdAndOrganizationId(productId, orgId)
                    .orElseThrow(() -> {
                        log.warn("Product not found productId={} orgId={}", productId, orgId);
                        return new RuntimeException("Product not found");
                    });

            return toResponse(product);
        } catch (RuntimeException e) {
            throw e; // Already logged above
        } catch (Exception e) {
            log.error("Error fetching product productId={} orgId={}", productId, orgId, e);
            throw new RuntimeException("Failed to fetch product", e);
        }
    }

    /**
     * Create a new product and automatically create Inventory and InventoryLog.
     */
    @Override
    public ProductResponseDTO createProduct(ProductCreateDTO dto) {
        try {
            log.info("Creating product for orgId={} productName={}", dto.getOrganizationId(), dto.getProductName());

            Boolean status = dto.getStatus() != null ? dto.getStatus() : Boolean.TRUE;

            Product product = Product.builder()
                    .organizationId(dto.getOrganizationId())
                    .productName(dto.getProductName())
                    .price(dto.getPrice())
                    .status(status)
                    .tankCapacity(dto.getTankCapacity())
                    .description(dto.getDescription())
                    .supplier(dto.getSupplier())
                    .currentLevel(dto.getCurrentLevel() != null ? dto.getCurrentLevel() : BigDecimal.ZERO)
                    .metric(dto.getMetric())
                    .lastUpdated(LocalDateTime.now())
                    .build();

            Product savedProduct = productRepository.save(product);
            log.debug("Product created successfully productId={} orgId={}", savedProduct.getId(), dto.getOrganizationId());

            // ---- CALL INVENTORY SERVICE FOR AUTO INSERT ----
            InventoryCreateDTO invDto = InventoryCreateDTO.builder()
                    .organizationId(savedProduct.getOrganizationId())
                    .productId(savedProduct.getId())
                    .productName(savedProduct.getProductName())
                    .totalCapacity(savedProduct.getTankCapacity())
                    .stockValue(savedProduct.getPrice() != null ? BigDecimal.valueOf(savedProduct.getPrice()) : BigDecimal.ZERO)
                    .lastUpdated(LocalDateTime.now())
                    .currentLevel(savedProduct.getCurrentLevel())
                    .metric(savedProduct.getMetric())
                    .status(savedProduct.getStatus())
                    .tankCapacity(savedProduct.getTankCapacity())
                    .empId(dto.getEmpId()) // or employeeId as per your DTO
                    .build();

            inventoryService.createInventory(invDto);
            log.debug("Inventory and InventoryLog automatically inserted for productId={}", savedProduct.getId());

            return toResponse(savedProduct);

        } catch (Exception e) {
            log.error("Error creating product orgId={} productName={}", dto.getOrganizationId(), dto.getProductName(), e);
            throw new RuntimeException("Failed to create product", e);
        }
    }

    /**
     * Update Product.
     */
    @Override
    public ProductResponseDTO updateProduct(String orgId, String productId, ProductUpdateDTO dto) {
        try {
            log.info("Updating productId={} for orgId={}", productId, orgId);

            Product product = productRepository.findByIdAndOrganizationId(productId, orgId)
                    .orElseThrow(() -> {
                        log.warn("Product not found for update productId={} orgId={}", productId, orgId);
                        return new RuntimeException("Product not found");
                    });

            product.setProductName(dto.getProductName());
            product.setPrice(dto.getPrice());
            product.setStatus(dto.getStatus() != null ? dto.getStatus() : product.getStatus());
            product.setTankCapacity(dto.getTankCapacity());
            product.setDescription(dto.getDescription());
            product.setSupplier(dto.getSupplier());
            product.setCurrentLevel(dto.getCurrentLevel());
            product.setMetric(dto.getMetric());
            product.setLastUpdated(LocalDateTime.now());

            Product updatedProduct = productRepository.save(product);
            log.debug("Product updated successfully productId={} orgId={}", updatedProduct.getId(), orgId);

            // -- UPDATE Inventory entity's currentLevel field --
            List<Inventory> inventories = inventoryService.getInventoriesByProductAndOrg(orgId, productId); // You need this method!
            for (Inventory inv : inventories) {
                inv.setCurrentLevel(updatedProduct.getCurrentLevel());
                inventoryService.saveInventory(inv); // persist the change in Inventory entity

                // -- UPDATE matching InventoryLog entity's currentLevel field --
                InventoryLog inventoryLog = inventoryService.getInventoryLogByInventoryId(inv.getInventoryId());
                if (inventoryLog != null) {
                    inventoryLog.setCurrentLevel(updatedProduct.getCurrentLevel());
                    inventoryLogRepository.save(inventoryLog);
                    log.debug("InventoryLog updated for inventoryId={} with new currentLevel={}", inv.getInventoryId(), updatedProduct.getCurrentLevel());
                }
            }

            return toResponse(updatedProduct);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating product productId={} orgId={}", productId, orgId, e);
            throw new RuntimeException("Failed to update product", e);
        }
    }

    /**
     * Delete a product by ID for a specific organization.
     */
    @Override
    public void deleteProduct(String orgId, String productId) {
        try {
            log.info("Deleting productId={} for orgId={}", productId, orgId);

            Product product = productRepository.findByIdAndOrganizationId(productId, orgId)
                    .orElseThrow(() -> {
                        log.warn("Product not found for delete productId={} orgId={}", productId, orgId);
                        return new RuntimeException("Product not found");
                    });

            productRepository.delete(product);
            log.debug("Product deleted successfully productId={} orgId={}", productId, orgId);
        } catch (RuntimeException e) {
            throw e; // Already logged
        } catch (Exception e) {
            log.error("Error deleting product productId={} orgId={}", productId, orgId, e);
            throw new RuntimeException("Failed to delete product", e);
        }
    }

    /**
     * Convert Product entity to ProductResponseDTO.
     */
    private ProductResponseDTO toResponse(Product p) {
        return ProductResponseDTO.builder()
                .id(p.getId())
                .organizationId(p.getOrganizationId())
                .productName(p.getProductName())
                .price(p.getPrice())
                .status(p.getStatus())
                .tankCapacity(p.getTankCapacity())
                .description(p.getDescription())
                .supplier(p.getSupplier())
                .currentLevel(p.getCurrentLevel())
                .metric(p.getMetric())
                .empId(p.getEmpId())
                .lastUpdated(p.getLastUpdated())
                .build();
    }
}
