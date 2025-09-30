package com.pulse.fineflux.controller;

import com.pulse.fineflux.domain.InventoryLogCreateDTO;
import com.pulse.fineflux.domain.InventoryLogResponseDTO;
import com.pulse.fineflux.domain.InventoryLogUpdateDTO;
import com.pulse.fineflux.service.InventoryLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/inventory-logs")
@RequiredArgsConstructor
@Slf4j
public class InventoryLogController {

    private final InventoryLogService inventoryLogService;

    /**
     * Create a new inventory log
     */
    @PostMapping
    public ResponseEntity<InventoryLogResponseDTO> createLog(@RequestBody InventoryLogCreateDTO dto) {
        log.info("Received request to create inventory log for productId={}", dto.getProductId());
        try {
            InventoryLogResponseDTO response = inventoryLogService.createLog(dto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating inventory log for productId={}: {}", dto.getProductId(), e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Update an existing inventory log by ID
     */
    @PutMapping("/{id}")
    public ResponseEntity<InventoryLogResponseDTO> updateLog(
            @PathVariable String id,
            @RequestBody InventoryLogUpdateDTO dto) {
        log.info("Received request to update inventory log: id={}", id);
        try {
            InventoryLogResponseDTO response = inventoryLogService.updateLog(id, dto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating inventory log id={}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Get all inventory logs with optional filters for product name and date range
     * Example: /api/inventory-logs/search?productName=Oil&fromDate=2025-09-01&toDate=2025-09-30
     */
    @GetMapping("/search")
    public ResponseEntity<List<InventoryLogResponseDTO>> searchLogs(
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date toDate) {
        log.info("Received request to search inventory logs with productName={}, fromDate={}, toDate={}",
                productName, fromDate, toDate);
        try {
            List<InventoryLogResponseDTO> logs = inventoryLogService.searchLogs(productName, fromDate, toDate);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            log.error("Error searching inventory logs: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Get a single inventory log by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<InventoryLogResponseDTO> getLogById(@PathVariable String id) {
        log.info("Received request to fetch inventory log: id={}", id);
        try {
            InventoryLogResponseDTO log = inventoryLogService.getLogById(id);
            return ResponseEntity.ok(log);
        } catch (Exception e) {
            log.error("Error fetching inventory log id={}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Delete an inventory log by ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLog(@PathVariable String id) {
        log.info("Received request to delete inventory log: id={}", id);
        try {
            inventoryLogService.deleteLog(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting inventory log id={}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
}
