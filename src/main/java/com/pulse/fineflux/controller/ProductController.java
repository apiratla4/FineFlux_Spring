package com.pulse.fineflux.controller;

import com.pulse.fineflux.domain.ProductCreateDTO;
import com.pulse.fineflux.domain.ProductResponseDTO;
import com.pulse.fineflux.domain.ProductUpdateDTO;
import com.pulse.fineflux.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/organizations/{orgId}/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // GET all products
    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> list(@PathVariable String orgId) {
        try {
            log.info("Fetching all products for orgId={}", orgId);
            List<ProductResponseDTO> products = productService.getAllProducts(orgId);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Error fetching products for orgId={}", orgId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // GET a single product
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponseDTO> get(@PathVariable String orgId, @PathVariable String productId) {
        try {
            log.info("Fetching productId={} for orgId={}", productId, orgId);
            ProductResponseDTO product = productService.getProduct(orgId, productId);
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            log.warn("Product not found productId={} orgId={}", productId, orgId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            log.error("Error fetching product productId={} orgId={}", productId, orgId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // POST create product
    @PostMapping
    public ResponseEntity<ProductResponseDTO> create(
            @PathVariable String orgId,
            @Valid @RequestBody ProductCreateDTO dto) {
        try {
            dto.setOrganizationId(orgId);
            log.info("Creating product for orgId={} productName={}", orgId, dto.getProductName());
            ProductResponseDTO product = productService.createProduct(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(product);
        } catch (Exception e) {
            log.error("Error creating product for orgId={} productName={}", orgId, dto.getProductName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // PUT update product
    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponseDTO> update(
            @PathVariable String orgId,
            @PathVariable String productId,
            @Valid @RequestBody ProductUpdateDTO dto) {
        try {
            log.info("Updating productId={} for orgId={}", productId, orgId);
            ProductResponseDTO updatedProduct = productService.updateProduct(orgId, productId, dto);
            return ResponseEntity.ok(updatedProduct);
        } catch (RuntimeException e) {
            log.warn("Product not found for update productId={} orgId={}", productId, orgId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            log.error("Error updating productId={} orgId={}", productId, orgId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // DELETE product
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> delete(@PathVariable String orgId, @PathVariable String productId) {
        try {
            log.info("Deleting productId={} for orgId={}", productId, orgId);
            productService.deleteProduct(orgId, productId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.warn("Product not found for delete productId={} orgId={}", productId, orgId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error deleting productId={} orgId={}", productId, orgId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
