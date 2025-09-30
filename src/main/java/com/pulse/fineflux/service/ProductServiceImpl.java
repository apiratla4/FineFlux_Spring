package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.ProductCreateDTO;
import com.pulse.fineflux.domain.ProductResponseDTO;
import com.pulse.fineflux.domain.ProductUpdateDTO;
import com.pulse.fineflux.entity.Product;
import com.pulse.fineflux.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    /**
     * Get all products for a specific organization.

     */
    @Override
    public List<ProductResponseDTO> getAllProducts(String orgId) {
        try {
            log.info("Fetching all products for orgId={}", orgId);

            // Fetch from repository and map to DTO
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
     * Create a new product.
     */
    @Override
    public ProductResponseDTO createProduct(ProductCreateDTO dto) {
        try {
            log.info("Creating product for orgId={} productName={}", dto.getOrganizationId(), dto.getProductName());

            // Build the entity
            Product product = Product.builder()
                    .organizationId(dto.getOrganizationId())
                    .productName(dto.getProductName())
                    .price(dto.getPrice())
                    .status(dto.getStatus())
                    .tankCapacity(dto.getTankCapacity())
                    .description(dto.getDescription())
                    .supplier(dto.getSupplier())
                    .currentLevel(dto.getCurrentLevel())
                    .metric(dto.getMetric())
                    .build();

            Product savedProduct = productRepository.save(product);
            log.debug("Product created successfully productId={} orgId={}", savedProduct.getId(), dto.getOrganizationId());

            return toResponse(savedProduct);
        } catch (Exception e) {
            log.error("Error creating product orgId={} productName={}", dto.getOrganizationId(), dto.getProductName(), e);
            throw new RuntimeException("Failed to create product", e);
        }
    }

    /**
     * Update an existing product.
     * @return ProductResponseDTO of the updated product.
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

            // Update fields
            product.setProductName(dto.getProductName());
            product.setPrice(dto.getPrice());
            product.setStatus(dto.getStatus());
            product.setTankCapacity(dto.getTankCapacity());
            product.setDescription(dto.getDescription());
            product.setSupplier(dto.getSupplier());
            product.setCurrentLevel(dto.getCurrentLevel());
            product.setMetric(dto.getMetric());

            Product updatedProduct = productRepository.save(product);
            log.debug("Product updated successfully productId={} orgId={}", updatedProduct.getId(), orgId);

            return toResponse(updatedProduct);
        } catch (RuntimeException e) {
            throw e; // Already logged
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
                .build();
    }
}
