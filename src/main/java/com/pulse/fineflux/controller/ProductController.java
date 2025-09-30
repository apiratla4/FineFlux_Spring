package com.pulse.fineflux.controller;

import com.pulse.fineflux.domain.ProductCreateDTO;
import com.pulse.fineflux.domain.ProductResponseDTO;
import com.pulse.fineflux.domain.ProductUpdateDTO;
import com.pulse.fineflux.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(@RequestBody ProductCreateDTO dto) {
        log.info("Received request to create product: {}", dto.getProductName());
        try {
            ProductResponseDTO response = productService.createProduct(dto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating product {}: {}", dto.getProductName(), e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @PathVariable String productId,
            @RequestBody ProductUpdateDTO dto) {
        log.info("Received request to update product: productId={}", productId);
        try {
            ProductResponseDTO response = productService.updateProduct(productId, dto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating product {}: {}", productId, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable String productId) {
        log.info("Received request to fetch product: productId={}", productId);
        try {
            ProductResponseDTO response = productService.getProductById(productId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching product {}: {}", productId, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts() {
        log.info("Received request to fetch all products");
        try {
            List<ProductResponseDTO> products = productService.getAllProducts();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Error fetching all products: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String productId) {
        log.info("Received request to delete product: productId={}", productId);
        try {
            productService.deleteProduct(productId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting product {}: {}", productId, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
}
