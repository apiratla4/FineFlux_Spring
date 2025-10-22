
package com.pulse.fineflux.controller;

import com.pulse.fineflux.domain.DocumentCreateRequest;
import com.pulse.fineflux.domain.DocumentResponse;
import com.pulse.fineflux.domain.DocumentUpdateRequest;
import com.pulse.fineflux.service.DocumentService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/organizations/{orgId}/documents")
public class DocumentController {

    private final DocumentService service;

    public DocumentController(DocumentService service) {
        this.service = service;
    }

    // GET all for org (paged)
    @GetMapping
    public Page<DocumentResponse> list(@PathVariable("orgId") String orgId, Pageable pageable) {
        log.debug("HTTP GET documents orgId={} page={} size={}", orgId, pageable.getPageNumber(), pageable.getPageSize());
        return service.list(orgId, pageable);
    }

    // GET by id within org
    @GetMapping("/{id}")
    public DocumentResponse get(@PathVariable("orgId") String orgId, @PathVariable String id) {
        log.debug("HTTP GET documents/{id} id={} orgId={}", id, orgId);
        return service.get(orgId, id);
    }

    // POST create within org
    @PostMapping
    public DocumentResponse create(@PathVariable("orgId") String orgId, @Valid @RequestBody DocumentCreateRequest req) {
        log.info("HTTP POST documents orgId={}", orgId);
        return service.create(orgId, req);
    }

    // PUT update by id within org
    @PutMapping("/{id}")
    public DocumentResponse update(@PathVariable("orgId") String orgId, @PathVariable String id, @Valid @RequestBody DocumentUpdateRequest req) {
        log.info("HTTP PUT documents/{id} id={} orgId={}", id, orgId);
        return service.update(orgId, id, req);
    }

    // DELETE by id within org
    @DeleteMapping("/{id}")
    public void delete(@PathVariable("orgId") String orgId, @PathVariable String id) {
        log.info("HTTP DELETE documents/{id} id={} orgId={}", id, orgId);
        service.delete(orgId, id);
    }
}
