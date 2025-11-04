package com.pulse.fineflux.controller;

import com.pulse.fineflux.entity.Product;
import com.pulse.fineflux.repository.ProductRepository;
import com.pulse.fineflux.service.ProductPriceUpdateService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/organizations/{orgId}/appsettings")
@RequiredArgsConstructor
public class AppSettingsController {

    private final ProductRepository productRepository;
    private final ProductPriceUpdateService priceUpdateService;


    // PUT /api/organizations/{orgId}/appsettings/products/{productId}/price?price=120.0&empId=EMP123

    @PutMapping("/products/{productId}/price")
    public ResponseEntity<Void> updateProductPrice(@PathVariable("orgId") String orgId,
                                                   @PathVariable("productId") String productId,
                                                   @RequestParam("price") double price,
                                                   @RequestParam("empId") @NotBlank String empId) {
        log.info("HTTP PUT /api/organizations/{}/appsettings/products/{}/price price={} empId={}",
                orgId, productId, price, empId);

        // Guard to ensure product belongs to the requested organization
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
        if (!orgId.equals(product.getOrganizationId())) {
            throw new RuntimeException("Product does not belong to organizationId=" + orgId);
        }

        priceUpdateService.updateProductPrice(productId, price, empId);
        return ResponseEntity.ok().build();
    }

}
