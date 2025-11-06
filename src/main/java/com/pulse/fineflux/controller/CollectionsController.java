package com.pulse.fineflux.controller;

import com.pulse.fineflux.domain.*;
import com.pulse.fineflux.service.CollectionsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/organizations/{orgId}/collections")
@RequiredArgsConstructor
public class CollectionsController {

    private final CollectionsService collectionsService;

    @PostMapping
    public ResponseEntity<CollectionsResponseDTO> create(
            @PathVariable String orgId,
            @RequestBody CollectionsCreateDTO dto) {
        try {
            dto.setOrganizationId(orgId);
            CollectionsResponseDTO created = collectionsService.create(dto);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            log.error("Error creating collection for orgId={}: {}", orgId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<CollectionsResponseDTO> update(
            @PathVariable String id,
            @RequestBody CollectionsUpdateDTO dto) {
        try {
            CollectionsResponseDTO updated = collectionsService.update(id, dto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error updating collection id={}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }


    @GetMapping("/by-sale/{saleId}")
    public ResponseEntity<CollectionsResponseDTO> getBySaleId(@PathVariable String saleId) {
        try {
            CollectionsResponseDTO dto = collectionsService.getBySaleId(saleId);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            log.error("Error fetching collection by saleId={}: {}", saleId, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping
    public ResponseEntity<List<CollectionsResponseDTO>> getAll(@PathVariable String orgId) {
        try {
            List<CollectionsResponseDTO> list = collectionsService.getAll(orgId);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            log.error("Error getting all collections for orgId={}: {}", orgId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable String orgId,
            @PathVariable String id) {
        try {
            collectionsService.delete(id);
            return ResponseEntity.noContent().build(); // 204 [web:110][web:101]
        } catch (RuntimeException ex) {
            log.warn("Collection not found for delete id={} orgId={}: {}", id, orgId, ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404 [web:110]
        } catch (Exception e) {
            log.error("Error deleting collection id={} orgId={}: {}", id, orgId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500 [web:110]
        }
    }
}

