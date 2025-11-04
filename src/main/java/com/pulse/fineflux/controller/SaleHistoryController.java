package com.pulse.fineflux.controller;


import com.pulse.fineflux.domain.SaleHistoryResponseDTO;
import com.pulse.fineflux.service.SaleHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/organizations/{orgId}/sale-history")
@RequiredArgsConstructor
public class SaleHistoryController {

    private final SaleHistoryService saleHistoryService;

    // Get all SaleHistory for org
    @GetMapping
    public ResponseEntity<List<SaleHistoryResponseDTO>> getAll(@PathVariable String orgId) {
        List<SaleHistoryResponseDTO> results = saleHistoryService.getAll(orgId);
        return ResponseEntity.ok(results);
    }

    // Get SaleHistory between dates
    @GetMapping("/by-date")
    public ResponseEntity<List<SaleHistoryResponseDTO>> getByDate(
            @PathVariable String orgId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        List<SaleHistoryResponseDTO> results = saleHistoryService.getByDateRange(orgId, from, to);
        return ResponseEntity.ok(results);
    }
}
