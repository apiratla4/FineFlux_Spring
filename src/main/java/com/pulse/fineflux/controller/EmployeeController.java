// src/main/java/com/pulse/fineflux/controller/EmployeeController.java
package com.pulse.fineflux.controller;

import com.pulse.fineflux.domain.EmployeeCreateRequest;
import com.pulse.fineflux.domain.EmployeeResponse;
import com.pulse.fineflux.domain.EmployeeUpdateRequest;
import com.pulse.fineflux.domain.ChangePasswordRequest;
import org.springframework.http.ResponseEntity;
import com.pulse.fineflux.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/organizations/{orgId}/employees")
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponse create(@PathVariable("orgId") String orgId, @Valid @RequestBody EmployeeCreateRequest req) {
        log.info("HTTP POST employees orgId={} empId={} username={}", orgId, req.empId, req.username);
        return service.create(orgId, req);
    }

    @GetMapping("/{id}")
    public EmployeeResponse get(@PathVariable("orgId") String orgId, @PathVariable String id) {
        log.debug("HTTP GET employees/{id} id={} orgId={}", id, orgId);
        return service.get(orgId, id);
    }

    @GetMapping
    public Page<EmployeeResponse> list(@PathVariable("orgId") String orgId, Pageable pageable) {
        log.debug("HTTP GET employees orgId={} page={} size={}", orgId, pageable.getPageNumber(), pageable.getPageSize());
        return service.list(orgId, pageable);
    }

    @PutMapping("/{id}")
    public EmployeeResponse update(@PathVariable("orgId") String orgId, @PathVariable String id, @Valid @RequestBody EmployeeUpdateRequest req) {
        log.info("HTTP PUT employees/{id} id={} orgId={}", id, orgId);
        return service.update(orgId, id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("orgId") String orgId, @PathVariable String id) {
        log.info("HTTP DELETE employees/{id} id={} orgId={}", id, orgId);
        service.delete(orgId, id);
    }

    /**
     * Change employee password
     * PUT /api/organizations/{orgId}/employees/{id}/change-password
     */
    @PutMapping("/{id}/change-password")
    public ResponseEntity<String> changePassword(
            @PathVariable("orgId") String orgId,
            @PathVariable("id") String id,
            @Valid @RequestBody ChangePasswordRequest req
    ) {
        log.info("HTTP PUT employees/{id}/change-password id={} orgId={}", id, orgId);
        service.changePassword(orgId, id, req);
        return ResponseEntity.ok("Password changed successfully");
    }
}