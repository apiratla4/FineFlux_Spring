package com.pulse.fineflux.controller;

import com.pulse.fineflux.domain.ProfitLossResponseDTO;
import com.pulse.fineflux.service.ProfitLossService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/organizations/{orgId}/profit-loss")
@RequiredArgsConstructor
public class ProfitLossController {

    private final ProfitLossService profitLossService;

    /**
     * Trigger Profit/Loss calculation manually
     */
    @PostMapping("/calculate")
    public ProfitLossResponseDTO calculateProfitLoss(@PathVariable String orgId) {
        log.info("Calculating Profit/Loss for orgId={}", orgId);
        return profitLossService.calculateAndSaveProfitLoss(orgId);
    }

    /**
     * Fetch all historical Profit/Loss records
     */
    @GetMapping("/all")
    public List<ProfitLossResponseDTO> getAllProfitLoss(@PathVariable String orgId) {
        return profitLossService.getAllProfitLoss();
    }
}
