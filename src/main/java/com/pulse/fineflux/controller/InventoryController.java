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

@Slf4j
@RestController
@RequestMapping("/api/organizations/{orgId}/inventories")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<InventoryResponseDTO> create(
            @PathVariable String orgId,
            @RequestBody InventoryCreateDTO dto) {
        try {
            dto.setOrganizationId(orgId);
            log.info("Creating inventory for orgId={}, productId={}", orgId, dto.getProductId());
            InventoryResponseDTO response = inventoryService.createInventory(dto);
            log.debug("Inventory created successfully inventoryId={}", response.getInventoryId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating inventory for orgId={}", orgId, e);
            return ResponseEntity.status(500).build();
        }
    }

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

    @DeleteMapping("/{inventoryId}")
    public ResponseEntity<Void> delete(
            @PathVariable String orgId,
            @PathVariable String inventoryId,
            @RequestHeader("X-Employee-Id") String employeeId
    ) {
        try {
            log.info("Deleting inventory inventoryId={} for orgId={}", inventoryId, orgId);
            inventoryService.deleteInventory(orgId, inventoryId, employeeId);
            log.debug("Inventory deleted successfully inventoryId={}", inventoryId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting inventory inventoryId={} orgId={}", inventoryId, orgId, e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/{productId}/latest")
    public ResponseEntity<InventoryResponseDTO> getLatestInventory(
            @PathVariable String orgId,
            @PathVariable String productId) {
        try {
            log.info("Fetching latest inventory for orgId={}, productId={}", orgId, productId);
            InventoryResponseDTO response = inventoryService.getLatestInventory(orgId, productId);
            log.debug("Fetched latest inventory for orgId={}, productId={}", orgId, productId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching latest inventory for orgId={} productId={}", orgId, productId, e);
            return ResponseEntity.status(404).build();
        }
    }
}
