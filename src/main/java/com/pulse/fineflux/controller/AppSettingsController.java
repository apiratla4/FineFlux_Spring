package com.pulse.fineflux.controller;

import com.pulse.fineflux.service.ProductPriceUpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController

public class AppSettingsController {

    @Autowired
    private ProductPriceUpdateService priceUpdateService;

    @PostMapping("/product/{id}/update-price")
    public ResponseEntity<Void> updateProductPrice(@PathVariable String id,
                                                   @RequestParam double price,
                                                   @RequestParam String empId) {
        priceUpdateService.updateProductPrice(id, price, empId);
        return ResponseEntity.ok().build();
    }
}
