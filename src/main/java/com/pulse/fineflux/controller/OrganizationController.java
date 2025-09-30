// src/main/java/com/pulse/fineflux/controller/OrganizationController.java
package com.pulse.fineflux.controller;

import com.pulse.fineflux.domain.OrganizationCreateRequest;
import com.pulse.fineflux.domain.OrganizationResponse;
import com.pulse.fineflux.domain.OrganizationUpdateRequest;
import com.pulse.fineflux.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    private final OrganizationService service;

    public OrganizationController(OrganizationService service) {
        this.service = service;
    }

    // GET all (paged)
    @GetMapping
    public Page<OrganizationResponse> list(Pageable pageable) {
        log.debug("HTTP GET /api/organizations page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        return service.list(pageable);
    }

    // GET by Mongo _id
    @GetMapping("/{id}")
    public OrganizationResponse get(@PathVariable String id) {
        log.debug("HTTP GET /api/organizations/{}", id);
        return service.getById(id);
    }

    // GET by business key
    @GetMapping("/by-org-id/{orgId}")
    public OrganizationResponse getByOrgId(@PathVariable("orgId") String orgId) {
        log.debug("HTTP GET /api/organizations/by-org-id/{}", orgId);
        return service.getByOrgId(orgId);
    }

    // POST create
    @PostMapping
    public OrganizationResponse create(@Valid @RequestBody OrganizationCreateRequest req) {
        log.info("HTTP POST /api/organizations orgId={}", req.organizationId);
        return service.create(req);
    }

    // PUT update by Mongo _id
    @PutMapping("/{id}")
    public OrganizationResponse update(@PathVariable String id, @Valid @RequestBody OrganizationUpdateRequest req) {
        log.info("HTTP PUT /api/organizations/{}", id);
        return service.update(id, req);
    }

    // DELETE by Mongo _id
    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        log.info("HTTP DELETE /api/organizations/{}", id);
        service.delete(id);
    }
}
