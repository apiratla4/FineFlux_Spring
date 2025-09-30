// src/main/java/com/pulse/fineflux/controller/CustomerController.java
package com.pulse.fineflux.controller;

import com.pulse.fineflux.domain.CustomerCreateRequest;
import com.pulse.fineflux.domain.CustomerResponse;
import com.pulse.fineflux.domain.CustomerUpdateRequest;
import com.pulse.fineflux.service.CustomerService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/organizations/{orgId}/customers")
public class CustomerController {

    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    @GetMapping
    public Page<CustomerResponse> list(@PathVariable("orgId") String orgId, Pageable pageable) {
        log.debug("HTTP GET customers orgId={} page={} size={}", orgId, pageable.getPageNumber(), pageable.getPageSize());
        return service.list(orgId, pageable);
    }

    @GetMapping("/{id}")
    public CustomerResponse get(@PathVariable("orgId") String orgId, @PathVariable String id) {
        log.debug("HTTP GET customers/{id} id={} orgId={}", id, orgId);
        return service.get(orgId, id);
    }

    @PostMapping
    public CustomerResponse create(@PathVariable("orgId") String orgId, @Valid @RequestBody CustomerCreateRequest req) {
        log.info("HTTP POST customers orgId={}", orgId);
        return service.create(orgId, req);
    }

    @PutMapping("/{id}")
    public CustomerResponse update(@PathVariable("orgId") String orgId, @PathVariable String id, @Valid @RequestBody CustomerUpdateRequest req) {
        log.info("HTTP PUT customers/{id} id={} orgId={}", id, orgId);
        return service.update(orgId, id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("orgId") String orgId, @PathVariable String id) {
        log.info("HTTP DELETE customers/{id} id={} orgId={}", id, orgId);
        service.delete(orgId, id);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllForOrg(@PathVariable("orgId") String orgId) {
        log.warn("HTTP DELETE customers (bulk) orgId={}", orgId);
        service.deleteAllForOrganization(orgId);
        return ResponseEntity.noContent().build();
    }
}
