// src/main/java/com/pulse/fineflux/controller/CustomerController.java
package com.pulse.fineflux.controller;

import com.pulse.fineflux.domain.CustomerCreateRequest;
import com.pulse.fineflux.domain.CustomerHistoryCreateRequest;
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
    public CustomerResponse create(@PathVariable("orgId") String orgId,
                                   @Valid @RequestBody CustomerCreateRequest req) {
        CustomerResponse created = customerService.create(orgId, req);
        if (created.amountBorrowed != null && created.amountBorrowed.signum() > 0) {
            CustomerHistoryCreateRequest hReq = new CustomerHistoryCreateRequest();
            hReq.custId = created.custId;
            // If your backend expects +payment and -borrow, ensure this sign matches domain rules.
            // UI sends borrow as negative; keep consistency service-side as well.
            hReq.transactionAmount = created.amountBorrowed.negate(); // optional: align sign convention
            hReq.transactionDate = java.time.Instant.now();
            hReq.notes = "Opening balance on customer creation";
            historyService.addTransaction(orgId, hReq);
        }
        return created;
    }

    @PutMapping("/{id}")
    public CustomerResponse update(@PathVariable("orgId") String orgId,
                                   @PathVariable String id,
                                   @Valid @RequestBody CustomerUpdateRequest req) {
        return customerService.update(orgId, id, req);
    }

    // Existing: delete by internal id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("orgId") String orgId,
                                       @PathVariable String id) {
        customerService.delete(orgId, id);
        return ResponseEntity.noContent().build();
    }

    // New: delete by external custId used by UI
    @DeleteMapping("/by-cust/{custId}")
    public ResponseEntity<Void> deleteByCustId(@PathVariable("orgId") String orgId,
                                               @PathVariable String custId) {
        customerService.deleteByCustId(orgId, custId);
        return ResponseEntity.noContent().build();
    }

    // Optional: delete via query param (alternative pattern)
    // DELETE /api/organizations/{orgId}/customers?custId=ABC123
    @DeleteMapping(params = "custId")
    public ResponseEntity<Void> deleteByCustIdQuery(@PathVariable("orgId") String orgId,
                                                    @RequestParam("custId") String custId) {
        customerService.deleteByCustId(orgId, custId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllForOrg(@PathVariable("orgId") String orgId) {
        customerService.deleteAllForOrganization(orgId);
        return ResponseEntity.noContent().build();
    }
}
