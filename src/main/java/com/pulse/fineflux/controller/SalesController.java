package com.pulse.fineflux.controller;

import com.pulse.fineflux.domain.SalesCreateDTO;
import com.pulse.fineflux.domain.SalesResponseDTO;
import com.pulse.fineflux.domain.SalesUpdateDTO;
import com.pulse.fineflux.service.SalesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/organizations/{orgId}/sales")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SalesController {

    private final SalesService salesService;

    /**
     * Create a new sale
     * POST /api/organizations/{orgId}/sales
     */
    @PostMapping
    public ResponseEntity<SalesResponseDTO> createSale(@PathVariable String orgId, @RequestBody SalesCreateDTO dto) {
        try {
            dto.setOrganizationId(orgId);
            log.info("Request to create sale for orgId={} empId={}", orgId, dto.getEmpId());

            SalesResponseDTO response = salesService.createSale(dto);

            log.debug("Sale created successfully: saleId={}", response.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating sale for orgId={} empId={}: {}", orgId, dto.getEmpId(), e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Get all sales for organization
     * GET /api/organizations/{orgId}/sales
     */
    @GetMapping
    public ResponseEntity<List<SalesResponseDTO>> getAllSales(@PathVariable String orgId) {
        try {
            log.info("Request to get all sales for orgId={}", orgId);
            List<SalesResponseDTO> sales = salesService.getAllSales(orgId);
            log.debug("Fetched {} sales for orgId={}", sales.size(), orgId);
            return ResponseEntity.ok(sales);
        } catch (Exception e) {
            log.error("Error fetching sales for orgId={}: {}", orgId, e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Get sales by date range
     * GET /api/organizations/{orgId}/sales/by-date?from=2025-10-01T00:00:00&to=2025-10-31T23:59:59
     */
    @GetMapping("/by-date")
    public ResponseEntity<List<SalesResponseDTO>> getSalesByDateRange(
            @PathVariable String orgId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        try {
            log.info("Request to get sales by date range for orgId={} from={} to={}", orgId, from, to);
            List<SalesResponseDTO> sales = salesService.getSalesByDateRange(orgId, from, to);
            log.debug("Fetched {} sales for orgId={} in date range", sales.size(), orgId);
            return ResponseEntity.ok(sales);
        } catch (Exception e) {
            log.error("Error fetching sales by date for orgId={}: {}", orgId, e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Get a sale by its ID
     * GET /api/organizations/{orgId}/sales/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<SalesResponseDTO> getSaleById(@PathVariable String orgId, @PathVariable String id) {
        try {
            log.info("Request to get sale by id={} for orgId={}", id, orgId);
            SalesResponseDTO sale = salesService.getSaleById(id);
            log.debug("Fetched sale: saleId={}", sale.getId());
            return ResponseEntity.ok(sale);
        } catch (Exception e) {
            log.error("Error fetching sale id={} for orgId={}: {}", id, orgId, e.getMessage(), e);
            return ResponseEntity.status(404).body(null);
        }
    }

    /**
     * Update a sale
     * PUT /api/organizations/{orgId}/sales/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<SalesResponseDTO> updateSale(@PathVariable String orgId, @PathVariable String id,
                                                       @RequestBody SalesUpdateDTO dto) {
        try {
            log.info("Request to update sale id={} for orgId={}", id, orgId);
            SalesResponseDTO updated = salesService.updateSale(id, dto);
            log.debug("Sale updated successfully: saleId={}", updated.getId());
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error updating sale id={} for orgId={}: {}", id, orgId, e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Delete a sale
     * DELETE /api/organizations/{orgId}/sales/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSale(@PathVariable String orgId, @PathVariable String id) {
        try {
            log.info("Request to delete sale id={} for orgId={}", id, orgId);
            salesService.deleteSale(id);
            log.debug("Sale deleted successfully: saleId={}", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting sale id={} for orgId={}: {}", id, orgId, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
}
