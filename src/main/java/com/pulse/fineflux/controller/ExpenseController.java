package com.pulse.fineflux.controller;

import com.pulse.fineflux.domain.ExpenseCreateDTO;
import com.pulse.fineflux.domain.ExpenseResponseDTO;
import com.pulse.fineflux.domain.ExpenseUpdateDTO;
import com.pulse.fineflux.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/organizations/{orgId}/expenses")
@RequiredArgsConstructor
@Slf4j
public class ExpenseController {

    private final ExpenseService service;

    @PostMapping
    public ExpenseResponseDTO create(@PathVariable String orgId, @RequestBody ExpenseCreateDTO dto) {
        try {
            dto.setOrganizationId(orgId);
            log.info("Creating new Expense for org: {}", orgId);
            return service.create(dto);
        } catch (Exception e) {
            log.error("Error creating Expense for org {}: {}", orgId, e.getMessage(), e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    public ExpenseResponseDTO update(@PathVariable String orgId, @PathVariable String id,
                                     @RequestBody ExpenseUpdateDTO dto) {
        try {
            dto.setOrganizationId(orgId);
            log.info("Updating Expense {} for org: {}", id, orgId);
            return service.update(id, dto);
        } catch (Exception e) {
            log.error("Error updating Expense {} for org {}: {}", id, orgId, e.getMessage(), e);
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String orgId, @PathVariable String id) {
        try {
            log.info("Deleting Expense {} for org: {}", id, orgId);
            service.deleteByOrg(id, orgId);
        } catch (Exception e) {
            log.error("Error deleting Expense {} for org {}: {}", id, orgId, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ExpenseResponseDTO getById(@PathVariable String orgId, @PathVariable String id) {
        try {
            log.info("Fetching Expense {} for org: {}", id, orgId);
            return service.getByOrgAndId(orgId, id);
        } catch (Exception e) {
            log.error("Error fetching Expense {} for org {}: {}", id, orgId, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping
    public List<ExpenseResponseDTO> getAll(@PathVariable String orgId) {
        try {
            log.info("Fetching all Expenses for org: {}", orgId);
            return service.getAllByOrg(orgId);
        } catch (Exception e) {
            log.error("Error fetching Expenses for org {}: {}", orgId, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/search/employee")
    public List<ExpenseResponseDTO> searchByEmployeeName(
            @PathVariable String orgId,
            @RequestParam String employeeName) {
        try {
            log.info("Searching Expenses by employeeName '{}' for org: {}", employeeName, orgId);
            return service.searchByEmployeeName(orgId, employeeName);
        } catch (Exception e) {
            log.error("Error searching Expenses by employeeName for org {}: {}", orgId, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/search/category")
    public List<ExpenseResponseDTO> searchByCategory(
            @PathVariable String orgId,
            @RequestParam String categoryName) {
        try {
            log.info("Searching Expenses by categoryName '{}' for org: {}", categoryName, orgId);
            return service.searchByCategory(orgId, categoryName);
        } catch (Exception e) {
            log.error("Error searching Expenses by categoryName for org {}: {}", orgId, e.getMessage(), e);
            throw e;
        }
    }
    @GetMapping("/search/date/range")
    public List<ExpenseResponseDTO> searchByDateRange(
            @PathVariable String orgId,
            @RequestParam String from,
            @RequestParam String to) {
        try {
            log.info("Searching Expenses by expenseDate in range [{}, {}] for org: {}", from, to, orgId);
            return service.searchByExpenseDateRange(orgId, LocalDate.parse(from), LocalDate.parse(to));
        } catch (Exception e) {
            log.error("Error searching Expenses by expenseDate range for org {}: {}", orgId, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/search/employee/all")
    public List<String> getAllEmployeeNames(@PathVariable String orgId) {
        try {
            log.info("Fetching all employee names for org: {}", orgId);
            return service.getAllEmployeeNames(orgId);
        } catch (Exception e) {
            log.error("Error fetching all employee names for org {}: {}", orgId, e.getMessage(), e);
            throw e;
        }
    }
}
