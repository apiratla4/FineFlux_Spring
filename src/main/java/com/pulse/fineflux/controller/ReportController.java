package com.pulse.fineflux.controller;

import com.pulse.fineflux.domain.ReportRequest;
import com.pulse.fineflux.domain.ReportResponse;
import com.pulse.fineflux.service.ReportingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/organizations/{organizationId}/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportingService reportingService;

    @PostMapping
    public ReportResponse<?> getReport(
            @PathVariable String organizationId,
            @RequestBody ReportRequest request) {
        request.setOrganizationId(organizationId);
        return reportingService.getReport(request);
    }

    @GetMapping("/sales")
    public ReportResponse<?> getSalesReport(
            @PathVariable String organizationId,
            @RequestParam String reportType,
            @RequestParam(required = false) LocalDate day,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to
    ) {
        ReportRequest req = ReportRequest.builder()
                .entityName("SALES")
                .reportType(reportType)
                .day(day)
                .year(year)
                .month(month)
                .from(from)
                .to(to)
                .organizationId(organizationId)
                .build();
        return reportingService.getReport(req);
    }

    @GetMapping("/inventory")
    public ReportResponse<?> getInventoryReport(
            @PathVariable String organizationId,
            @RequestParam String reportType,
            @RequestParam(required = false) LocalDate day,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to
    ) {
        ReportRequest req = ReportRequest.builder()
                .entityName("INVENTORY")
                .reportType(reportType)
                .day(day)
                .year(year)
                .month(month)
                .from(from)
                .to(to)
                .organizationId(organizationId)
                .build();
        return reportingService.getReport(req);
    }

    @GetMapping("/customer")
    public ReportResponse<?> getCustomerReport(
            @PathVariable String organizationId,
            @RequestParam String reportType,
            @RequestParam(required = false) LocalDate day,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to
    ) {
        ReportRequest req = ReportRequest.builder()
                .entityName("CUSTOMER")
                .reportType(reportType)
                .day(day)
                .year(year)
                .month(month)
                .from(from)
                .to(to)
                .organizationId(organizationId)
                .build();
        return reportingService.getReport(req);
    }

    @GetMapping("/employee")
    public ReportResponse<?> getEmployeeReport(
            @PathVariable String organizationId,
            @RequestParam String reportType,
            @RequestParam(required = false) LocalDate day,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to
    ) {
        ReportRequest req = ReportRequest.builder()
                .entityName("EMPLOYEE")
                .reportType(reportType)
                .day(day)
                .year(year)
                .month(month)
                .from(from)
                .to(to)
                .organizationId(organizationId)
                .build();
        return reportingService.getReport(req);
    }

    @GetMapping("/financesummary")
    public ReportResponse<?> getFinanceSummaryReport(
            @PathVariable String organizationId,
            @RequestParam String reportType,
            @RequestParam(required = false) LocalDate day,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to
    ) {
        ReportRequest req = ReportRequest.builder()
                .entityName("FINANCESUMMARY")
                .reportType(reportType)
                .day(day)
                .year(year)
                .month(month)
                .from(from)
                .to(to)
                .organizationId(organizationId)
                .build();
        return reportingService.getReport(req);
    }
}
