package com.pulse.fineflux.controller;

import com.pulse.fineflux.domain.CustomerCreateRequest;
import com.pulse.fineflux.domain.CustomerResponse;
import com.pulse.fineflux.domain.CustomerUpdateRequest;
import com.pulse.fineflux.service.CustomerService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/organizations/{orgId}/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    // ========== BASIC CRUD ==========

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
        return customerService.create(orgId, req);
    }

    @PutMapping("/{id}")
    public CustomerResponse update(@PathVariable("orgId") String orgId,
                                   @PathVariable String id,
                                   @Valid @RequestBody CustomerUpdateRequest req) {
        return customerService.update(orgId, id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("orgId") String orgId,
                                       @PathVariable String id) {
        customerService.delete(orgId, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/by-cust/{custId}")
    public ResponseEntity<Void> deleteByCustId(@PathVariable("orgId") String orgId,
                                               @PathVariable String custId) {
        customerService.deleteByCustId(orgId, custId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllForOrg(@PathVariable("orgId") String orgId) {
        customerService.deleteAllForOrganization(orgId);
        return ResponseEntity.noContent().build();
    }

    // ========== ✅ NEW: DATE FILTERS ==========

    // GET /api/organizations/{orgId}/customers/filter/today
    @GetMapping("/filter/today")
    public Page<CustomerResponse> listToday(@PathVariable("orgId") String orgId, Pageable pageable) {
        log.info("Fetching today's customers for orgId={}", orgId);
        return customerService.listToday(orgId, pageable);
    }

    // GET /api/organizations/{orgId}/customers/filter/week
    @GetMapping("/filter/week")
    public Page<CustomerResponse> listThisWeek(@PathVariable("orgId") String orgId, Pageable pageable) {
        log.info("Fetching this week's customers for orgId={}", orgId);
        return customerService.listThisWeek(orgId, pageable);
    }

    // GET /api/organizations/{orgId}/customers/filter/month
    @GetMapping("/filter/month")
    public Page<CustomerResponse> listThisMonth(@PathVariable("orgId") String orgId, Pageable pageable) {
        log.info("Fetching this month's customers for orgId={}", orgId);
        return customerService.listThisMonth(orgId, pageable);
    }

    // GET /api/organizations/{orgId}/customers/filter/date?date=2025-10-22
    @GetMapping("/filter/date")
    public Page<CustomerResponse> listByDate(
            @PathVariable("orgId") String orgId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Pageable pageable) {
        log.info("Fetching customers by date for orgId={} date={}", orgId, date);
        return customerService.listByDate(orgId, date, pageable);
    }

    // GET /api/organizations/{orgId}/customers/filter/range?startDate=2025-10-01&endDate=2025-10-31
    @GetMapping("/filter/range")
    public Page<CustomerResponse> listByDateRange(
            @PathVariable("orgId") String orgId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable) {
        log.info("Fetching customers by date range for orgId={} from={} to={}", orgId, startDate, endDate);
        return customerService.listByDateRange(orgId, startDate, endDate, pageable);
    }
}
