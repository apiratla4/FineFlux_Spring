package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.ProductCreateDTO;
import com.pulse.fineflux.domain.ProductResponseDTO;
import com.pulse.fineflux.domain.ProductUpdateDTO;
import com.pulse.fineflux.entity.Product;
import com.pulse.fineflux.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * Create a new product
     */
    @Transactional
    public ProductResponseDTO createProduct(ProductCreateDTO dto) {
        log.info("Creating product: {}", dto.getProductName());
        try {
            Product product = Product.builder()
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
            log.info("Product created successfully: productId={}", savedProduct.getProductId());

            return mapToResponseDTO(savedProduct);
        } catch (Exception e) {
            log.error("Failed to create product {}: {}", dto.getProductName(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Update existing product
     */
    @Transactional
    public ProductResponseDTO updateProduct(String productId, ProductUpdateDTO dto) {
        log.info("Updating product: productId={}", productId);
        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

            product.setProductName(dto.getProductName());
            product.setPrice(dto.getPrice());
            product.setStatus(dto.getStatus());
            product.setTankCapacity(dto.getTankCapacity());
            product.setDescription(dto.getDescription());
            product.setSupplier(dto.getSupplier());
            product.setCurrentLevel(dto.getCurrentLevel());
            product.setMetric(dto.getMetric());

            Product updatedProduct = productRepository.save(product);
            log.info("Product updated successfully: productId={}", updatedProduct.getProductId());

            return mapToResponseDTO(updatedProduct);
        } catch (Exception e) {
            log.error("Failed to update product {}: {}", productId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get product by ID
     */
    public ProductResponseDTO getProductById(String productId) {
        log.info("Fetching product: productId={}", productId);
        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
            return mapToResponseDTO(product);
        } catch (Exception e) {
            log.error("Failed to fetch product {}: {}", productId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get all products
     */
    public List<ProductResponseDTO> getAllProducts() {
        log.info("Fetching all products");
        try {
            return productRepository.findAll()
                    .stream()
                    .map(this::mapToResponseDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to fetch all products: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Delete product by ID
     */
    @Transactional
    public void deleteProduct(String productId) {
        log.info("Deleting product: productId={}", productId);
        try {
            if (!productRepository.existsById(productId)) {
                log.warn("Product not found for deletion: productId={}", productId);
                throw new RuntimeException("Product not found: " + productId);
            }
            productRepository.deleteById(productId);
            log.info("Product deleted successfully: productId={}", productId);
        } catch (Exception e) {
            log.error("Failed to delete product {}: {}", productId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Map Product entity to Response DTO
     */
    private ProductResponseDTO mapToResponseDTO(Product product) {
        return ProductResponseDTO.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .price(product.getPrice())
                .status(product.getStatus())
                .tankCapacity(product.getTankCapacity())
                .description(product.getDescription())
                .supplier(product.getSupplier())
                .currentLevel(product.getCurrentLevel())
                .metric(product.getMetric())
                .build();
    }
}
