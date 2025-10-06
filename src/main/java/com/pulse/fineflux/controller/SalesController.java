package com.pulse.fineflux.controller;

import com.pulse.fineflux.domain.SalesCreateDTO;
import com.pulse.fineflux.domain.SalesResponseDTO;
import com.pulse.fineflux.domain.SalesUpdateDTO;
import com.pulse.fineflux.service.SalesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/{orgId}/sales")
@RequiredArgsConstructor
public class SalesController {

    private final SalesService salesService;

    /**
     * Create a new Sale
     */
    @PostMapping
    public ResponseEntity<SalesResponseDTO> createSale(
            @PathVariable String orgId,
            @RequestBody SalesCreateDTO dto) {
        try {
            dto.setOrganizationId(orgId);
            log.info("Creating Sale for orgId={} employeeId={}", orgId, dto.getEmployeeId());

            SalesResponseDTO response = salesService.createSale(dto);

            log.debug("Sale created successfully id={}", response.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating Sale for orgId={} employeeId={}", orgId, dto.getEmployeeId(), e);
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Update existing Sale
     */
    @PutMapping("/{id}")
    public ResponseEntity<SalesResponseDTO> updateSale(
            @PathVariable String orgId,
            @PathVariable String id,
            @RequestBody SalesUpdateDTO dto) {
        try {
            log.info("Updating Sale id={} for orgId={}", id, orgId);

            SalesResponseDTO updated = salesService.updateSale(id, dto);

            log.debug("Sale updated successfully id={}", updated.getId());
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error updating Sale id={} orgId={}", id, orgId, e);
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Delete Sale
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSale(@PathVariable String orgId, @PathVariable String id) {
        try {
            log.info("Deleting Sale id={} for orgId={}", id, orgId);

            salesService.deleteSale(id);

            log.debug("Sale deleted successfully id={}", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting Sale id={} orgId={}", id, orgId, e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Get Sale by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<SalesResponseDTO> getSaleById(@PathVariable String orgId, @PathVariable String id) {
        try {
            log.info("Fetching Sale id={} for orgId={}", id, orgId);

            SalesResponseDTO sale = salesService.getSaleById(id);

            log.debug("Fetched Sale id={}", sale.getId());
            return ResponseEntity.ok(sale);
        } catch (Exception e) {
            log.error("Error fetching Sale id={} orgId={}", id, orgId, e);
            return ResponseEntity.status(404).body(null);
        }
    }

    /**
     * Get all Sales for an organization
     */
    @GetMapping
    public ResponseEntity<List<SalesResponseDTO>> getAllSales(@PathVariable String orgId) {
        try {
            log.info("Fetching all Sales for orgId={}", orgId);

            List<SalesResponseDTO> salesList = salesService.getAllSales(orgId);

            log.debug("Fetched {} Sales for orgId={}", salesList.size(), orgId);
            return ResponseEntity.ok(salesList);
        } catch (Exception e) {
            log.error("Error fetching Sales for orgId={}", orgId, e);
            return ResponseEntity.status(500).body(null);
        }
    }
}
