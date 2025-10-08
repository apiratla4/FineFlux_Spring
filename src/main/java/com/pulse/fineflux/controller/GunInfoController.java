package com.pulse.fineflux.controller;

import com.pulse.fineflux.domain.*;
import com.pulse.fineflux.service.GunInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizations/{orgId}/guninfo")
@RequiredArgsConstructor
@Slf4j
public class GunInfoController {

    private final GunInfoService gunInfoService;

    /**
     * Create new GunInfo
     */
    @PostMapping
    public ResponseEntity<GunInfoResponseDTO> createGunInfo(
            @PathVariable String orgId,
            @RequestBody GunInfoCreateDTO dto) {
        dto.setOrganizationId(orgId);
        log.info("API request: createGunInfo for orgId={}", orgId);
        return ResponseEntity.ok(gunInfoService.createGunInfo(dto));
    }

    /**
     * Update existing GunInfo
     */
    @PutMapping("/{id}")
    public ResponseEntity<GunInfoResponseDTO> updateGunInfo(
            @PathVariable String orgId,
            @PathVariable String id,
            @RequestBody GunInfoUpdateDTO dto) {
        dto.setOrganizationId(orgId);
        log.info("API request: updateGunInfo ID={} orgId={}", id, orgId);
        return ResponseEntity.ok(gunInfoService.updateGunInfo(id, dto));
    }

    /**
     * Delete GunInfo
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGunInfo(
            @PathVariable String orgId,
            @PathVariable String id) {
        log.info("API request: deleteGunInfo ID={} orgId={}", id, orgId);
        gunInfoService.deleteGunInfo(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get GunInfo by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<GunInfoResponseDTO> getGunInfoById(
            @PathVariable String orgId,
            @PathVariable String id) {
        log.info("API request: getGunInfoById ID={} orgId={}", id, orgId);
        return ResponseEntity.ok(gunInfoService.getGunInfoById(id));
    }

    /**
     * Get all GunInfo for organization
     */
    @GetMapping
    public ResponseEntity<List<GunInfoResponseDTO>> getAllGunInfo(@PathVariable String orgId) {
        log.info("API request: getAllGunInfo for orgId={}", orgId);
        return ResponseEntity.ok(gunInfoService.getAllGunInfo(orgId));
    }
}
