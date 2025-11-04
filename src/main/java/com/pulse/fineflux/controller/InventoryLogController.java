package com.pulse.fineflux.controller;

import com.pulse.fineflux.domain.InventoryLogResponseDTO;
import com.pulse.fineflux.service.InventoryLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/organizations/{orgId}/inventory-logs")
@RequiredArgsConstructor
public class InventoryLogController {

    private final InventoryLogService inventoryLogService;

    // Get all inventory logs for an organization
    @GetMapping
    public ResponseEntity<List<InventoryLogResponseDTO>> getAllLogs(@PathVariable String orgId) {
        List<InventoryLogResponseDTO> logs = inventoryLogService.getAllLogs(orgId);
        return ResponseEntity.ok(logs);
    }

    // Get a single inventory log by MongoDB _id
    @GetMapping("/{id}")
    public ResponseEntity<InventoryLogResponseDTO> getLogById(@PathVariable String orgId, @PathVariable String id) {
        try {
            InventoryLogResponseDTO logEntry = inventoryLogService.getLogById(orgId, id);
            return ResponseEntity.ok(logEntry);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete single inventory log by MongoDB id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLog(@PathVariable String orgId, @PathVariable String id) {
        inventoryLogService.deleteLog(orgId, id);
        return ResponseEntity.noContent().build();
    }

    // Get all logs by product name for organization (ignoring date)
    @GetMapping("/by-product")
    public ResponseEntity<List<InventoryLogResponseDTO>> getLogsByProductName(
            @PathVariable String orgId,
            @RequestParam String productName) {
        List<InventoryLogResponseDTO> logs = inventoryLogService.getLogsByProductName(orgId, productName);
        return ResponseEntity.ok(logs);
    }
}
