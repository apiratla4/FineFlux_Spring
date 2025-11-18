package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.ReportRequest;
import com.pulse.fineflux.domain.ReportResponse;
import com.pulse.fineflux.entity.*;
import com.pulse.fineflux.repository.*;
import lombok.*;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportingService {

    private final SalesRepository salesRepo;
    private final InventoryRepository inventoryRepo;
    private final CustomerRepository customerRepo;
    private final EmployeeRepository employeeRepo;
    private final FinanceSummaryRepository financeRepo;

    public ReportResponse<?> getReport(ReportRequest req) {
        switch (req.getEntityName().toUpperCase()) {
            case "SALES":
                return getSalesReport(req);
            case "INVENTORY":
                return getInventoryReport(req);
            case "CUSTOMER":
                return getCustomerReport(req);
            case "EMPLOYEE":
                return getEmployeeReport(req);
            case "FINANCESUMMARY":
                return getFinanceReport(req);
            default:
                throw new IllegalArgumentException("Unknown entity: " + req.getEntityName());
        }
    }

    // SALES REPORT
    private ReportResponse<Sales> getSalesReport(ReportRequest req) {
        LocalDateTime start, end;
        if ("DAY".equalsIgnoreCase(req.getReportType())) {
            start = req.getDay().atStartOfDay();
            end = req.getDay().atTime(LocalTime.MAX);
        } else if ("MONTH".equalsIgnoreCase(req.getReportType())) {
            start = LocalDate.of(req.getYear(), req.getMonth(), 1).atStartOfDay();
            end = start.plusMonths(1).minusNanos(1);
        } else {
            start = req.getFrom();
            end = req.getTo();
        }
        List<Sales> data = salesRepo.findAllByDateTimeBetweenAndOrganizationId(start, end, req.getOrganizationId());
        double totalLiters = data.stream().mapToDouble(Sales::getSalesInLiters).sum();
        double totalRupees = data.stream().mapToDouble(Sales::getSalesInRupees).sum();
        return ReportResponse.<Sales>builder()
                .entityName("SALES")
                .reportType(req.getReportType())
                .fromDate(start.toString())
                .toDate(end.toString())
                .totalRecords(data.size())
                .summary(new SalesSummary(totalLiters, totalRupees))
                .data(data)
                .build();
    }

    @Getter @Setter @AllArgsConstructor
    public static class SalesSummary {
        private double totalSalesInLiters;
        private double totalSalesInRupees;
    }

    // INVENTORY REPORT
    private ReportResponse<Inventory> getInventoryReport(ReportRequest req) {
        LocalDateTime start, end;
        if ("DAY".equalsIgnoreCase(req.getReportType())) {
            start = req.getDay().atStartOfDay();
            end = req.getDay().atTime(LocalTime.MAX);
        } else if ("MONTH".equalsIgnoreCase(req.getReportType())) {
            start = LocalDate.of(req.getYear(), req.getMonth(), 1).atStartOfDay();
            end = start.plusMonths(1).minusNanos(1);
        } else {
            start = req.getFrom();
            end = req.getTo();
        }
        List<Inventory> data = inventoryRepo.findAllByLastUpdatedBetweenAndOrganizationId(start, end, req.getOrganizationId());
        double totalStockValue = data.stream().map(inv -> inv.getStockValue() != null ? inv.getStockValue().doubleValue() : 0.0).reduce(0.0, Double::sum);
        double totalCurrentLevel = data.stream().map(inv -> inv.getCurrentLevel() != null ? inv.getCurrentLevel().doubleValue() : 0.0).reduce(0.0, Double::sum);
        return ReportResponse.<Inventory>builder()
                .entityName("INVENTORY")
                .reportType(req.getReportType())
                .fromDate(start.toString())
                .toDate(end.toString())
                .totalRecords(data.size())
                .summary(new InventorySummary(totalStockValue, totalCurrentLevel))
                .data(data)
                .build();
    }

    @Getter @Setter @AllArgsConstructor
    public static class InventorySummary {
        private double totalStockValue;
        private double totalCurrentLevel;
    }

    // CUSTOMER REPORT
    private ReportResponse<Customer> getCustomerReport(ReportRequest req) {
        LocalDate start, end;
        if ("DAY".equalsIgnoreCase(req.getReportType())) {
            start = req.getDay();
            end = req.getDay();
        } else if ("MONTH".equalsIgnoreCase(req.getReportType())) {
            start = LocalDate.of(req.getYear(), req.getMonth(), 1);
            end = start.plusMonths(1).minusDays(1);
        } else {
            start = req.getFrom().toLocalDate();
            end = req.getTo().toLocalDate();
        }
        List<Customer> data = customerRepo.findAllByBorrowDateBetweenAndOrganizationId(start, end, req.getOrganizationId());
        double totalBorrowed = data.stream().map(c -> c.getTotalBorrowedAmount() != null ? c.getTotalBorrowedAmount().doubleValue() : 0.0).reduce(0.0, Double::sum);
        return ReportResponse.<Customer>builder()
                .entityName("CUSTOMER")
                .reportType(req.getReportType())
                .fromDate(start.toString())
                .toDate(end.toString())
                .totalRecords(data.size())
                .summary(new CustomerSummary(totalBorrowed))
                .data(data)
                .build();
    }

    @Getter @Setter @AllArgsConstructor
    public static class CustomerSummary {
        private double totalBorrowedAmount;
    }

    // EMPLOYEE REPORT
    private ReportResponse<Employee> getEmployeeReport(ReportRequest req) {
        Instant start, end;
        if ("DAY".equalsIgnoreCase(req.getReportType())) {
            start = req.getDay().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
            end = req.getDay().atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant();
        } else if ("MONTH".equalsIgnoreCase(req.getReportType())) {
            start = LocalDate.of(req.getYear(), req.getMonth(), 1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
            end = start.atZone(ZoneId.systemDefault()).plusMonths(1).minusNanos(1).toInstant();
        } else {
            start = req.getFrom().atZone(ZoneId.systemDefault()).toInstant();
            end = req.getTo().atZone(ZoneId.systemDefault()).toInstant();
        }
        List<Employee> data = employeeRepo.findAllByJoinedDateBetweenAndOrganizationId(start, end, req.getOrganizationId());
        long activeCount = data.stream().filter(emp -> "ACTIVE".equalsIgnoreCase(emp.getStatus())).count();
        long inactiveCount = data.size() - activeCount;
        return ReportResponse.<Employee>builder()
                .entityName("EMPLOYEE")
                .reportType(req.getReportType())
                .fromDate(start.toString())
                .toDate(end.toString())
                .totalRecords(data.size())
                .summary(new EmployeeSummary(activeCount, inactiveCount))
                .data(data)
                .build();
    }

    @Getter @Setter @AllArgsConstructor
    public static class EmployeeSummary {
        private long activeCount;
        private long inactiveCount;
    }

    // FINANCE SUMMARY REPORT
    private ReportResponse<FinanceSummary> getFinanceReport(ReportRequest req) {
        LocalDateTime start, end;
        if ("DAY".equalsIgnoreCase(req.getReportType())) {
            start = req.getDay().atStartOfDay();
            end = req.getDay().atTime(LocalTime.MAX);
        } else if ("MONTH".equalsIgnoreCase(req.getReportType())) {
            start = LocalDate.of(req.getYear(), req.getMonth(), 1).atStartOfDay();
            end = start.plusMonths(1).minusNanos(1);
        } else {
            start = req.getFrom();
            end = req.getTo();
        }
        List<FinanceSummary> data = financeRepo.findAllByCreatedAtBetweenAndOrganizationId(start, end, req.getOrganizationId());
        double totalCash = data.stream().mapToDouble(FinanceSummary::getCashReceived).sum();
        double totalExpenses = data.stream().mapToDouble(FinanceSummary::getTotalExpenses).sum();
        return ReportResponse.<FinanceSummary>builder()
                .entityName("FINANCESUMMARY")
                .reportType(req.getReportType())
                .fromDate(start.toString())
                .toDate(end.toString())
                .totalRecords(data.size())
                .summary(new FinanceSummaryData(totalCash, totalExpenses))
                .data(data)
                .build();
    }

    @Getter @Setter @AllArgsConstructor
    public static class FinanceSummaryData {
        private double totalCash;
        private double totalExpenses;
    }
}
