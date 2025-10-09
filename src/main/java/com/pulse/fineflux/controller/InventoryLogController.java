package com.pulse.fineflux.controller;

import com.pulse.fineflux.domain.InventoryLogResponseDTO;
import com.pulse.fineflux.service.InventoryLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/organizations/{orgId}/inventory-logs")
@RequiredArgsConstructor
public class InventoryLogController {

    private final InventoryLogService inventoryLogService;

    /**
     * Get all inventory logs for an organization
     */
    @GetMapping
    public ResponseEntity<List<InventoryLogResponseDTO>> getAll(@PathVariable String orgId) {
        try {
            log.info("Fetching all inventory logs for orgId={}", orgId);
            List<InventoryLogResponseDTO> logs = inventoryLogService.getAllLogs(orgId);
            log.debug("Fetched {} inventory logs for orgId={}", logs.size(), orgId);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            log.error("Error fetching inventory logs for orgId={}", orgId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get a single inventory log by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<InventoryLogResponseDTO> getById(@PathVariable String orgId, @PathVariable String id) {
        try {
            log.info("Fetching inventory log with id={} for orgId={}", id, orgId);
            InventoryLogResponseDTO logEntry = inventoryLogService.getLogById(orgId, id);
            log.debug("Fetched inventory log id={} successfully", id);
            return ResponseEntity.ok(logEntry);
        } catch (Exception e) {
            log.error("Error fetching inventory log id={} for orgId={}", id, orgId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Search inventory logs with optional filters
     */
    @GetMapping("/search")
    public ResponseEntity<List<InventoryLogResponseDTO>> search(
            @PathVariable String orgId,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fromDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date toDate) {
        try {
            log.info("Searching inventory logs for orgId={} with filters productName={}, fromDate={}, toDate={}",
                    orgId, productName, fromDate, toDate);
            List<InventoryLogResponseDTO> logs = inventoryLogService.searchLogs(orgId, productName, fromDate, toDate);
            log.debug("Found {} inventory logs matching search criteria for orgId={}", logs.size(), orgId);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            log.error("Error searching inventory logs for orgId={} with filters productName={}, fromDate={}, toDate={}",
                    orgId, productName, fromDate, toDate, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
