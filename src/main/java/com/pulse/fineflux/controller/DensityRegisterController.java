package com.pulse.fineflux.controller;


import com.pulse.fineflux.domain.DensityRegisterDTO;
import com.pulse.fineflux.domain.DensityRegisterUpdateDTO;
import com.pulse.fineflux.domain.DensityRegisterResponseDTO;
import com.pulse.fineflux.service.DensityRegisterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/organizations/{orgId}/density-register")
@RequiredArgsConstructor
public class DensityRegisterController {

    private final DensityRegisterService densityRegisterService;

    @PostMapping
    public DensityRegisterResponseDTO create(
            @PathVariable String orgId,
            @RequestBody DensityRegisterDTO dto
    ) {
        log.info("API: Create DensityRegister orgId={}", orgId);
        dto.setOrganizationId(orgId);
        return densityRegisterService.create(dto);
    }

    @PutMapping("/{id}")
    public DensityRegisterResponseDTO update(
            @PathVariable String orgId,
            @PathVariable String id,
            @RequestBody DensityRegisterUpdateDTO dto
    ) {
        log.info("API: Update DensityRegister id={} orgId={}", id, orgId);
        return densityRegisterService.update(id, orgId, dto);
    }

    @GetMapping("/{id}")
    public DensityRegisterResponseDTO getById(
            @PathVariable String orgId,
            @PathVariable String id
    ) {
        log.info("API: Get DensityRegister id={} orgId={}", id, orgId);
        return densityRegisterService.getByIdForOrg(id, orgId);
    }

    @GetMapping
    public List<DensityRegisterResponseDTO> getAllForOrg(@PathVariable String orgId) {
        log.info("API: Get all DensityRegister for orgId={}", orgId);
        return densityRegisterService.getByOrganizationId(orgId);
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable String orgId,
            @PathVariable String id
    ) {
        log.info("API: Delete DensityRegister id={} orgId={}", id, orgId);
        densityRegisterService.delete(id, orgId);
    }
}
