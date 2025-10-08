// src/main/java/com/pulse/fineflux/controller/CustomerHistoryController.java
package com.pulse.fineflux.controller;

import com.pulse.fineflux.domain.CustomerHistoryCreateRequest;
import com.pulse.fineflux.domain.CustomerHistoryResponse;
import com.pulse.fineflux.service.CustomerHistoryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organizations/{orgId}/customers/history")
public class CustomerHistoryController {

    private final CustomerHistoryService service;

    public CustomerHistoryController(CustomerHistoryService service) {
        this.service = service;
    }

    // POST-only transactions
    @PostMapping
    public CustomerHistoryResponse add(@PathVariable("orgId") String orgId, @Valid @RequestBody CustomerHistoryCreateRequest req) {
        return service.addTransaction(orgId, req);
    }

    // All transactions via query param (kept for compatibility)
    @GetMapping
    public Page<CustomerHistoryResponse> list(@PathVariable("orgId") String orgId,
                                              @RequestParam("custId") String custId,
                                              Pageable pageable) {
        return service.listByCustomer(orgId, custId, pageable);
    }

    // New: All transactions via path variable
    @GetMapping("/cust/{custId}")
    public Page<CustomerHistoryResponse> listByCustId(@PathVariable("orgId") String orgId,
                                                      @PathVariable("custId") String custId,
                                                      Pageable pageable) {
        return service.listByCustomer(orgId, custId, pageable);
    }

    // Latest N transactions, default 3
    @GetMapping("/latest")
    public Page<CustomerHistoryResponse> latest(@PathVariable("orgId") String orgId,
                                                @RequestParam("custId") String custId,
                                                @RequestParam(name = "limit", defaultValue = "3") int limit) {
        return service.latestN(orgId, custId, limit);
    }
}
