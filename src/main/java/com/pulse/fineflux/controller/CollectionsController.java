package com.pulse.fineflux.controller;

import com.pulse.fineflux.domain.*;
import com.pulse.fineflux.service.CollectionsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @GetMapping("/{id}")
    public ResponseEntity<CollectionsResponseDTO> getById(@PathVariable String id) {
        try {
            CollectionsResponseDTO dto = collectionsService.getById(id);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            log.error("Error getting collection by id={}: {}", id, e.getMessage(), e);
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
    public ResponseEntity<Void> delete(@PathVariable String id) {
        try {
            collectionsService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting collection id={}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

