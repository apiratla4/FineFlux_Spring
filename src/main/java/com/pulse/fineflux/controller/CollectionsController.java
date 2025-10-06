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
@RequestMapping("/api/{orgId}/collections")
@RequiredArgsConstructor
public class CollectionsController {

    private final CollectionsService collectionsService;

    @PostMapping
    public ResponseEntity<CollectionsResponseDTO> create(
            @PathVariable String orgId,
            @RequestBody CollectionsCreateDTO dto) {
        dto.setOrganizationId(orgId);
        return ResponseEntity.ok(collectionsService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CollectionsResponseDTO> update(
            @PathVariable String id,
            @RequestBody CollectionsUpdateDTO dto) {
        return ResponseEntity.ok(collectionsService.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CollectionsResponseDTO> getById(@PathVariable String id) {
        return ResponseEntity.ok(collectionsService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<CollectionsResponseDTO>> getAll(@PathVariable String orgId) {
        return ResponseEntity.ok(collectionsService.getAll(orgId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        collectionsService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
