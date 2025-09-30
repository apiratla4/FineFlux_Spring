package com.pulse.fineflux.controller;

import com.pulse.fineflux.entity.Inventory;
import com.pulse.fineflux.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventories")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

    private final InventoryService inventoryService;

    // Create inventory
    @PostMapping
    public ResponseEntity<Inventory> createInventory(@RequestBody Inventory inventory) {
        try {
            Inventory savedInventory = inventoryService.createInventory(inventory);
            return ResponseEntity.ok(savedInventory);
        } catch (Exception e) {
            log.error("Failed to create inventory: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    // Get all inventories
    @GetMapping
    public ResponseEntity<List<Inventory>> getAllInventories() {
        try {
            List<Inventory> inventories = inventoryService.getAllInventories();
            return ResponseEntity.ok(inventories);
        } catch (Exception e) {
            log.error("Failed to fetch inventories: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    // Update inventory by productId
    @PutMapping("/{productId}")
    public ResponseEntity<List<Inventory>> updateInventoryByProductId(
            @PathVariable String productId,
            @RequestBody Inventory inventory
    ) {
        try {
            List<Inventory> updatedInventories = inventoryService.updateInventoriesByProductId(productId, inventory);
            return ResponseEntity.ok(updatedInventories);
        } catch (Exception e) {
            log.error("Failed to update inventory: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

}
