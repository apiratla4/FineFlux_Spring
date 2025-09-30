package com.pulse.fineflux.controller;

import com.pulse.fineflux.domain.*;
import com.pulse.fineflux.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/{orgId}/inventories")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * Create a new inventory entry.
     */
    @PostMapping
    public ResponseEntity<InventoryResponseDTO> create(
            @PathVariable String orgId,
            @RequestBody InventoryCreateDTO dto) {
        try {
            dto.setOrganizationId(orgId);
            log.info("Creating inventory for orgId={}, productName={}", orgId, dto.getProductName());

            InventoryResponseDTO response = inventoryService.createInventory(dto);

            log.debug("Inventory created successfully inventoryId={}", response.getInventoryId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating inventory for orgId={}", orgId, e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Update existing inventory by product ID.
     */
    @PutMapping("/{productId}")
    public ResponseEntity<List<InventoryResponseDTO>> update(
            @PathVariable String orgId,
            @PathVariable String productId,
            @RequestBody InventoryUpdateDTO dto) {
        try {
            log.info("Updating inventory for orgId={}, productId={}", orgId, productId);

            List<InventoryResponseDTO> updatedList = inventoryService.updateInventory(orgId, productId, dto);

            log.debug("Inventory updated successfully for productId={}", productId);
            return ResponseEntity.ok(updatedList);
        } catch (Exception e) {
            log.error("Error updating inventory for orgId={}, productId={}", orgId, productId, e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * List all inventories for an organization.
     */
    @GetMapping
    public ResponseEntity<List<InventoryResponseDTO>> list(@PathVariable String orgId) {
        try {
            log.info("Fetching all inventories for orgId={}", orgId);

            List<InventoryResponseDTO> inventories = inventoryService.getAllInventories(orgId);

            log.debug("Fetched {} inventories for orgId={}", inventories.size(), orgId);
            return ResponseEntity.ok(inventories);
        } catch (Exception e) {
            log.error("Error fetching inventories for orgId={}", orgId, e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Delete inventory by inventory ID.
     */
    @DeleteMapping("/{inventoryId}")
    public ResponseEntity<Void> delete(
            @PathVariable String orgId,
            @PathVariable String inventoryId) {
        try {
            log.info("Deleting inventory inventoryId={} for orgId={}", inventoryId, orgId);

            inventoryService.deleteInventory(orgId, inventoryId);

            log.debug("Inventory deleted successfully inventoryId={}", inventoryId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting inventory inventoryId={} orgId={}", inventoryId, orgId, e);
            return ResponseEntity.status(500).build();
        }
    }
}
