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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

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
        try {
            return customerService.list(orgId, pageable);
        } catch (Exception e) {
            log.error("Error listing customers for orgId={}: {}", orgId, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    public CustomerResponse get(@PathVariable("orgId") String orgId, @PathVariable String id) {
        try {
            return customerService.get(orgId, id);
        } catch (Exception e) {
            log.error("Error fetching customer id={} orgId={}: {}", id, orgId, e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping
    public CustomerResponse create(@PathVariable("orgId") String orgId,
                                   @Valid @RequestBody CustomerCreateRequest req) {
        try {
            CustomerResponse created = customerService.create(orgId, req);
            if (created.amountBorrowed != null && created.amountBorrowed.signum() > 0) {
                CustomerHistoryCreateRequest hReq = new CustomerHistoryCreateRequest();
                hReq.custId = created.custId;
                hReq.transactionAmount = created.amountBorrowed.negate();
                hReq.transactionDate = java.time.Instant.now();
                hReq.notes = "Opening balance on customer creation";
                historyService.addTransaction(orgId, hReq);
            }
            return created;
        } catch (Exception e) {
            log.error("Error creating customer orgId={}: {}", orgId, e.getMessage(), e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    public CustomerResponse update(@PathVariable("orgId") String orgId,
                                   @PathVariable String id,
                                   @Valid @RequestBody CustomerUpdateRequest req) {
        try {
            return customerService.update(orgId, id, req);
        } catch (Exception e) {
            log.error("Error updating customer id={} orgId={}: {}", id, orgId, e.getMessage(), e);
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("orgId") String orgId,
                                       @PathVariable String id) {
        try {
            customerService.delete(orgId, id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting customer id={} orgId={}: {}", id, orgId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/by-cust/{custId}")
    public ResponseEntity<Void> deleteByCustId(@PathVariable("orgId") String orgId,
                                               @PathVariable String custId) {
        try {
            customerService.deleteByCustId(orgId, custId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting customer by custId={} orgId={}: {}", custId, orgId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping(params = "custId")
    public ResponseEntity<Void> deleteByCustIdQuery(@PathVariable("orgId") String orgId,
                                                    @RequestParam("custId") String custId) {
        try {
            customerService.deleteByCustId(orgId, custId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting customer by custId (query)={} orgId={}: {}", custId, orgId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllForOrg(@PathVariable("orgId") String orgId) {
        try {
            customerService.deleteAllForOrganization(orgId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error bulk deleting for orgId={}: {}", orgId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/today")
    public List<CustomerResponse> getToday(@PathVariable String orgId) {
        return customerService.getTodayCustomers(orgId);
    }

    @GetMapping("/week")
    public List<CustomerResponse> getWeek(@PathVariable String orgId) {
        return customerService.getWeekCustomers(orgId);
    }

    @GetMapping("/month")
    public List<CustomerResponse> getMonth(@PathVariable String orgId) {
        return customerService.getMonthCustomers(orgId);
    }

    @GetMapping("/range")
    public List<CustomerResponse> getRange(
            @PathVariable String orgId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return customerService.getCustomersByDateRange(orgId, from, to);
    }


    @PutMapping("/{id}/lifecycle-status")
    public CustomerResponse updateLifecycleStatus(
            @PathVariable("orgId") String orgId,
            @PathVariable String id,
            @Valid @RequestBody CustomerUpdateRequest req
    ) {
        try {
            return customerService.updateLifecycleStatus(orgId, id, req.lifecycleStatus);
        } catch (Exception e) {
            log.error("Error updating lifecycleStatus for customer id={} orgId={}: {}", id, orgId, e.getMessage(), e);
            throw e;
        }
    }
}

