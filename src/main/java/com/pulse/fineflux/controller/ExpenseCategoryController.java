package com.pulse.fineflux.controller;

import com.pulse.fineflux.domain.ExpenseCategoryCreateDTO;
import com.pulse.fineflux.domain.ExpenseCategoryResponseDTO;
import com.pulse.fineflux.domain.ExpenseCategoryUpdateDTO;
import com.pulse.fineflux.service.ExpenseCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizations/{orgId}/expense-categories")
@RequiredArgsConstructor
@Slf4j
public class ExpenseCategoryController {

    private final ExpenseCategoryService service;

    @PostMapping
    public ExpenseCategoryResponseDTO create(@PathVariable String orgId, @RequestBody ExpenseCategoryCreateDTO dto) {
        try {
            dto.setOrganizationId(orgId);
            log.info("Creating new ExpenseCategory for org: {}", orgId);
            return service.create(dto);
        } catch (Exception e) {
            log.error("Error creating ExpenseCategory for org {}: {}", orgId, e.getMessage(), e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    public ExpenseCategoryResponseDTO update(@PathVariable String orgId, @PathVariable String id,
                                             @RequestBody ExpenseCategoryUpdateDTO dto) {
        try {
            dto.setOrganizationId(orgId);
            log.info("Updating ExpenseCategory {} for org: {}", id, orgId);
            return service.update(id, dto);
        } catch (Exception e) {
            log.error("Error updating ExpenseCategory {} for org {}: {}", id, orgId, e.getMessage(), e);
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String orgId, @PathVariable String id) {
        try {
            log.info("Deleting ExpenseCategory {} for org: {}", id, orgId);
            service.deleteByOrg(id, orgId);
        } catch (Exception e) {
            log.error("Error deleting ExpenseCategory {} for org {}: {}", id, orgId, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ExpenseCategoryResponseDTO getById(@PathVariable String orgId, @PathVariable String id) {
        try {
            log.info("Fetching ExpenseCategory {} for org: {}", id, orgId);
            return service.getByOrgAndId(orgId, id);
        } catch (Exception e) {
            log.error("Error fetching ExpenseCategory {} for org {}: {}", id, orgId, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping
    public List<ExpenseCategoryResponseDTO> getAll(@PathVariable String orgId) {
        try {
            log.info("Fetching all ExpenseCategories for org: {}", orgId);
            return service.getAllByOrg(orgId);
        } catch (Exception e) {
            log.error("Error fetching ExpenseCategories for org {}: {}", orgId, e.getMessage(), e);
            throw e;
        }
    }
}
