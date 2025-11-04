package com.pulse.fineflux.controller;
import com.pulse.fineflux.domain.FinanceSummaryCreateDTO;
import com.pulse.fineflux.domain.FinanceSummaryResponseDTO;
import com.pulse.fineflux.domain.FinanceSummaryUpdateDTO;
import com.pulse.fineflux.service.FinanceSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizations/{orgId}/finance-summary")
@RequiredArgsConstructor
@Slf4j
public class FinanceSummaryController {

    private final FinanceSummaryService financeSummaryService;

    @PutMapping("/{id}")
    public FinanceSummaryResponseDTO update(@PathVariable String orgId, @PathVariable String id, @RequestBody FinanceSummaryUpdateDTO dto) {
        try {
            log.info("Updating FinanceSummary id={} orgId={}", id, orgId);
            // You may want to check orgId matches!
            return financeSummaryService.update(id, dto);
        } catch (Exception e) {
            log.error("Error updating FinanceSummary id={} orgId={}: {}", id, orgId, e.getMessage(), e);
            throw e;
        }
    }

    // Triggers the automatic summary generation
    @PostMapping("/auto")
    public FinanceSummaryResponseDTO autoCreate(@PathVariable String orgId) {
        try {
            log.info("Auto-creating FinanceSummary for orgId={}", orgId);
            return financeSummaryService.autoCreateFinanceSummary(orgId);
        } catch (Exception e) {
            log.error("Error auto-creating FinanceSummary for orgId={}: {}", orgId, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/latest")
    public FinanceSummaryResponseDTO getLatestByOrg(@PathVariable String orgId) {
        try {
            log.info("Fetching latest FinanceSummary for orgId={}", orgId);
            return financeSummaryService.getLatestByOrg(orgId);
        } catch (Exception e) {
            log.error("Error fetching latest FinanceSummary for orgId={}: {}", orgId, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping
    public List<FinanceSummaryResponseDTO> getAllByOrg(@PathVariable String orgId) {
        try {
            log.info("Fetching all FinanceSummaries for orgId={}", orgId);
            return financeSummaryService.getAllByOrg(orgId);
        } catch (Exception e) {
            log.error("Error fetching all FinanceSummaries for orgId={}: {}", orgId, e.getMessage(), e);
            throw e;
        }
    }
}
