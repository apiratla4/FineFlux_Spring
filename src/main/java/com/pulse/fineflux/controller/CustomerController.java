// src/main/java/com/pulse/fineflux/controller/CustomerController.java
package com.pulse.fineflux.controller;

import com.pulse.fineflux.domain.CustomerCreateRequest;
import com.pulse.fineflux.domain.CustomerResponse;
import com.pulse.fineflux.domain.CustomerUpdateRequest;
import com.pulse.fineflux.service.CustomerHistoryService;
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

    private final CustomerService customerService;
    private final CustomerHistoryService historyService;

    public CustomerController(CustomerService customerService, CustomerHistoryService historyService) {
        this.customerService = customerService;
        this.historyService = historyService;
    }

    @GetMapping
    public Page<CustomerResponse> list(@PathVariable("orgId") String orgId, Pageable pageable) {
        return customerService.list(orgId, pageable);
    }

    @GetMapping("/{id}")
    public CustomerResponse get(@PathVariable("orgId") String orgId, @PathVariable String id) {
        return customerService.get(orgId, id);
    }

    @PostMapping
    public CustomerResponse create(@PathVariable("orgId") String orgId, @Valid @RequestBody CustomerCreateRequest req) {
        // 1) create customer
        CustomerResponse created = customerService.create(orgId, req);

        // 2) auto-create initial history row with opening amount
        if (created.amountBorrowed != null && created.amountBorrowed.signum() > 0) {
            var hReq = new com.pulse.fineflux.domain.CustomerHistoryCreateRequest();
            hReq.custId = created.custId;
            hReq.transactionAmount = created.amountBorrowed;
            hReq.transactionDate = java.time.Instant.now();
            hReq.notes = "Opening balance on customer creation";
            historyService.addTransaction(orgId, hReq);
        }
        return created;
    }

    @PutMapping("/{id}")
    public CustomerResponse update(@PathVariable("orgId") String orgId, @PathVariable String id, @Valid @RequestBody CustomerUpdateRequest req) {
        return customerService.update(orgId, id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("orgId") String orgId, @PathVariable String id) {
        customerService.delete(orgId, id);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllForOrg(@PathVariable("orgId") String orgId) {
        customerService.deleteAllForOrganization(orgId);
        return ResponseEntity.noContent().build();
    }
}
