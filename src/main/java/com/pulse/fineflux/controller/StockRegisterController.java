package com.pulse.fineflux.controller;

import com.pulse.fineflux.domain.StockRegisterDTO;
import com.pulse.fineflux.domain.StockRegisterResponseDTO;
import com.pulse.fineflux.service.StockRegisterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/organizations/{orgId}/stock-register")
@RequiredArgsConstructor
public class StockRegisterController {

    private final StockRegisterService stockRegisterService;

    @PostMapping
    public StockRegisterResponseDTO create(
            @PathVariable String orgId,
            @RequestBody StockRegisterDTO dto
    ) {
        log.info("API: Create StockRegister orgId={}", orgId);
        dto.setOrganizationId(orgId);
        return stockRegisterService.create(dto);
    }

    @PutMapping("/{id}")
    public StockRegisterResponseDTO update(
            @PathVariable String orgId,
            @PathVariable String id,
            @RequestBody StockRegisterDTO dto
    ) {
        log.info("API: Update StockRegister id={} orgId={}", id, orgId);
        dto.setOrganizationId(orgId);
        return stockRegisterService.update(id, orgId, dto);
    }

    @GetMapping("/{id}")
    public StockRegisterResponseDTO getById(
            @PathVariable String orgId,
            @PathVariable String id
    ) {
        log.info("API: Get StockRegister id={} orgId={}", id, orgId);
        return stockRegisterService.getByIdForOrg(id, orgId);
    }

    @GetMapping
    public List<StockRegisterResponseDTO> getAllForOrg(@PathVariable String orgId) {
        log.info("API: Get all StockRegister for orgId={}", orgId);
        return stockRegisterService.getByOrganizationId(orgId);
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable String orgId,
            @PathVariable String id
    ) {
        log.info("API: Delete StockRegister id={} orgId={}", id, orgId);
        stockRegisterService.delete(id, orgId);
    }
}
