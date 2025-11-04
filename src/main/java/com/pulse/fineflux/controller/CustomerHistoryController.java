package com.pulse.fineflux.controller;

import com.pulse.fineflux.domain.CustomerHistoryCreateRequest;
import com.pulse.fineflux.domain.CustomerHistoryResponse;
import com.pulse.fineflux.service.CustomerHistoryService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@Slf4j
@RestController
@RequestMapping("/api/organizations/{orgId}/customers/history")
public class CustomerHistoryController {

    private final CustomerHistoryService service;

    public CustomerHistoryController(CustomerHistoryService service) {
        this.service = service;
    }

    @PostMapping
    public CustomerHistoryResponse add(@PathVariable("orgId") String orgId, @Valid @RequestBody CustomerHistoryCreateRequest req) {
        try {
            return service.addTransaction(orgId, req);
        } catch (Exception ex) {
            log.error("POST /api/organizations/{}/customers/history failed: {}", orgId, ex.getMessage(), ex);
            throw ex;
        }
    }

    @GetMapping("/cust/{custId}")
    public Page<CustomerHistoryResponse> listByCustId(@PathVariable("orgId") String orgId,
                                                      @PathVariable("custId") String custId,
                                                      Pageable pageable) {
        try {
            return service.listByCustomer(orgId, custId, pageable);
        } catch (Exception ex) {
            log.error("GET /api/organizations/{}/customers/history/cust/{} failed: {}", orgId, custId, ex.getMessage(), ex);
            throw ex;
        }
    }

    @GetMapping("/latest")
    public Page<CustomerHistoryResponse> latest(@PathVariable("orgId") String orgId,
                                                @RequestParam("custId") String custId,
                                                @RequestParam(name = "limit", defaultValue = "3") int limit) {
        try {
            return service.latestN(orgId, custId, limit);
        } catch (Exception ex) {
            log.error("GET /api/organizations/{}/customers/history/latest failed: orgId={}, custId={}, limit={}, error={}", orgId, custId, limit, ex.getMessage(), ex);
            throw ex;
        }
    }

    @GetMapping("/all")
    public Page<CustomerHistoryResponse> listAll(@PathVariable("orgId") String orgId, Pageable pageable) {
        try {
            return service.listByOrg(orgId, pageable);
        } catch (Exception ex) {
            log.error("GET /api/organizations/{}/customers/history/all failed: {}", orgId, ex.getMessage(), ex);
            throw ex;
        }
    }

    @GetMapping("/all/today")
    public Page<CustomerHistoryResponse> todayOrg(@PathVariable("orgId") String orgId, Pageable pageable) {
        try {
            return service.listByOrgToday(orgId, pageable);
        } catch (Exception ex) {
            log.error("GET /api/organizations/{}/customers/history/all/today failed: {}", orgId, ex.getMessage(), ex);
            throw ex;
        }
    }

    @GetMapping("/all/week")
    public Page<CustomerHistoryResponse> weekOrg(@PathVariable("orgId") String orgId, Pageable pageable) {
        try {
            return service.listByOrgLastWeek(orgId, pageable);
        } catch (Exception ex) {
            log.error("GET /api/organizations/{}/customers/history/all/week failed: {}", orgId, ex.getMessage(), ex);
            throw ex;
        }
    }

    @GetMapping("/all/month")
    public Page<CustomerHistoryResponse> monthOrg(@PathVariable("orgId") String orgId, Pageable pageable) {
        try {
            return service.listByOrgMonth(orgId, pageable);
        } catch (Exception ex) {
            log.error("GET /api/organizations/{}/customers/history/all/month failed: {}", orgId, ex.getMessage(), ex);
            throw ex;
        }
    }

    @GetMapping("/all/range")
    public Page<CustomerHistoryResponse> rangeOrg(@PathVariable("orgId") String orgId,
                                                  @RequestParam("from") Instant from,
                                                  @RequestParam("to") Instant to,
                                                  Pageable pageable) {
        try {
            return service.listByOrgDateRange(orgId, from, to, pageable);
        } catch (Exception ex) {
            log.error("GET /api/organizations/{}/customers/history/all/range failed: from={}, to={}, error={}", orgId, from, to, ex.getMessage(), ex);
            throw ex;
        }
    }

}
