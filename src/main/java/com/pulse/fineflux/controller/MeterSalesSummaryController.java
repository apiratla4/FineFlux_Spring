package com.pulse.fineflux.controller;

import com.pulse.fineflux.domain.MeterSalesDayDynamicDTO;
import com.pulse.fineflux.service.MeterSalesSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meter-sales")
public class MeterSalesSummaryController {

    private final MeterSalesSummaryService meterSalesSummaryService;

    /**
     * Get dynamic meter sales summary for a specific organization and product.
     * Guns are detected automatically from the data.
     * Example: GET /api/meter-sales/org/FOS-0051/summary?product=Petrol
     */
    @GetMapping("/org/{orgId}/summary")
    public List<MeterSalesDayDynamicDTO> getMeterSalesSummary(
            @PathVariable String orgId,
            @RequestParam String product
    ) {
        return meterSalesSummaryService.getMeterSalesSummary(orgId, product);
    }
}
