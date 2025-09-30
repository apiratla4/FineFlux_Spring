package com.pulse.fineflux.controller;

import com.pulse.fineflux.domain.InventoryCreateDTO;
import com.pulse.fineflux.domain.InventoryResponseDTO;
import com.pulse.fineflux.domain.InventoryUpdateDTO;
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

    @PostMapping
    public ResponseEntity<InventoryResponseDTO> createInventory(@RequestBody InventoryCreateDTO dto) {
        try {
            log.info("Received request to create inventory for productId={}", dto.getProductId());
            InventoryResponseDTO response = inventoryService.createInventory(dto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in createInventory: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping("/{productId}")
    public ResponseEntity<List<InventoryResponseDTO>> updateInventory(
            @PathVariable String productId,
            @RequestBody InventoryUpdateDTO dto) {
        try {
            log.info("Received request to update inventory for productId={}", productId);
            List<InventoryResponseDTO> response = inventoryService.updateInventory(productId, dto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in updateInventory for productId={}: {}", productId, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<InventoryResponseDTO>> getAllInventories() {
        try {
            log.info("Received request to fetch all inventories");
            List<InventoryResponseDTO> response = inventoryService.getAllInventories();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in getAllInventories: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
    @DeleteMapping("/{inventoryId}")
    public ResponseEntity<Void> deleteInventory(@PathVariable String inventoryId) {
        try {
            log.info("Received request to delete inventory: inventoryId={}", inventoryId);
            inventoryService.deleteInventory(inventoryId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error in deleteInventory: inventoryId={}, error={}", inventoryId, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

}
