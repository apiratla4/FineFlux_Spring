package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.*;
import com.pulse.fineflux.entity.Expense;
import com.pulse.fineflux.repository.EmployeeRepository;
import com.pulse.fineflux.repository.ExpenseCategoryRepository;
import com.pulse.fineflux.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository repo;
    private final ExpenseCategoryRepository catRepo;
    private final FinanceSummaryService financeSummaryService;
    private final EmployeeRepository employeeRepo;
    private final DateTimeService dateTimeService;

    @Override
    public ExpenseResponseDTO create(ExpenseCreateDTO dto) {
        try {
            if(!catRepo.findByCategoryNameAndOrganizationId(dto.getCategoryName(), dto.getOrganizationId()).isPresent())
                throw new RuntimeException("Category does not exist for org");
            Expense entity = Expense.builder()
                    .description(dto.getDescription())
                    .amount(dto.getAmount())
                    .categoryName(dto.getCategoryName())
                    .expenseDate(dto.getExpenseDate())
                    .createdAt(dateTimeService.nowLocal())
                    .organizationId(dto.getOrganizationId())
                    .empId(dto.getEmpId())
                    .employeeName(dto.getEmployeeName())
                    .build();
            Expense saved = repo.save(entity);

            try {
                log.info("Calling financeSummaryService.autoCreateFinanceSummary for orgId={} [action=expense]", saved.getOrganizationId());
                financeSummaryService.autoCreateFinanceSummary(saved.getOrganizationId());
                log.info("FinanceSummary successfully auto-created for orgId={} (expenses updated)", saved.getOrganizationId());
            } catch (Exception fsEx) {
                log.error("FinanceSummary auto-creation failed for orgId={} after expense: {}", saved.getOrganizationId(), fsEx.getMessage(), fsEx);
            }

            log.info("Expense created: {}", saved);
            return toResponse(saved);
        } catch(Exception e) {
            log.error("Error creating expense: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public ExpenseResponseDTO update(String id, ExpenseUpdateDTO dto) {
        try {
            Expense entity = repo.findByIdAndOrganizationId(id, dto.getOrganizationId())
                    .orElseThrow(() -> new RuntimeException("Expense not found (org)"));
            if(!catRepo.findByCategoryNameAndOrganizationId(dto.getCategoryName(), dto.getOrganizationId()).isPresent())
                throw new RuntimeException("Category does not exist for org");
            entity.setDescription(dto.getDescription());
            entity.setAmount(dto.getAmount());
            entity.setCategoryName(dto.getCategoryName());
            entity.setExpenseDate(dto.getExpenseDate());
            Expense saved = repo.save(entity);
            log.info("Expense updated: {}", saved);
            try {
                log.info("Calling financeSummaryService.autoupdatedFinanceSummary for orgId={} [action=expense]", saved.getOrganizationId());
                financeSummaryService.autoCreateFinanceSummary(saved.getOrganizationId());
                log.info("FinanceSummary successfully auto-updated for orgId={} (expenses updated)", saved.getOrganizationId());
            } catch (Exception fsEx) {
                log.error("FinanceSummary auto-creation failed for orgId={} after expense: {}", saved.getOrganizationId(), fsEx.getMessage(), fsEx);
            }
            return toResponse(saved);
        } catch(Exception e) {
            log.error("Error updating expense: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void deleteByOrg(String id, String organizationId) {
        try {
            Expense entity = repo.findByIdAndOrganizationId(id, organizationId)
                    .orElseThrow(() -> new RuntimeException("Expense not found (org)"));
            repo.deleteById(entity.getId());

            try {
                log.info("Calling financeSummaryService.autoCreateFinanceSummary for  delete time orgId={} [action=expense]", entity.getOrganizationId());
                financeSummaryService.autoCreateFinanceSummary(entity.getOrganizationId());
                log.info("FinanceSummary successfully auto-created for delete time orgId={} (expenses updated)", entity.getOrganizationId());
            } catch (Exception fsEx) {
                log.error("FinanceSummary auto-creation failed for orgId={} after expense: {}", entity.getOrganizationId(), fsEx.getMessage(), fsEx);
            }
            log.info("Expense deleted: {}", id);
        } catch(Exception e) {
            log.error("Error deleting expense: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public ExpenseResponseDTO getByOrgAndId(String organizationId, String id) {
        return repo.findByIdAndOrganizationId(id, organizationId)
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Expense not found (org)"));
    }

    @Override
    public List<ExpenseResponseDTO> getAllByOrg(String organizationId) {
        return repo.findByOrganizationId(organizationId).stream()
                .map(this::toResponse).toList();
    }

    @Override
    public List<ExpenseResponseDTO> searchByEmployeeName(String orgId, String employeeName) {
        try {
            return repo.findByOrganizationIdAndEmployeeNameContainingIgnoreCase(orgId, employeeName)
                    .stream().map(this::toResponse).toList();
        } catch (Exception e) {
            log.error("Error searching Expenses by employeeName '{}' for org '{}': {}", employeeName, orgId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<ExpenseResponseDTO> searchByCategory(String orgId, String categoryName) {
        try {
            return repo.findByOrganizationIdAndCategoryName(orgId, categoryName)
                    .stream().map(this::toResponse).toList();
        } catch (Exception e) {
            log.error("Error searching Expenses by categoryName '{}' for org '{}': {}", categoryName, orgId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<ExpenseResponseDTO> searchByExpenseDateRange(String orgId, LocalDate from, LocalDate to) {
        try {
            return repo.findByOrganizationIdAndExpenseDateBetween(orgId, from, to)
                    .stream().map(this::toResponse).toList();
        } catch (Exception e) {
            log.error("Error searching Expenses by expenseDate range for org {}: {}", orgId, e.getMessage(), e);
            throw e;
        }
    }
    @Override
    public List<String> getAllEmployeeNames(String orgId) {
        try {
            return employeeRepo.findByOrganizationId(orgId).stream()
                    .map(emp -> emp.getFirstName() + " " + emp.getLastName()) // or just emp.getFirstName() as needed
                    .toList();
        } catch (Exception e) {
            log.error("Error fetching all Employee names for org '{}': {}", orgId, e.getMessage(), e);
            throw e;
        }
    }

    private ExpenseResponseDTO toResponse(Expense entity) {
        return ExpenseResponseDTO.builder()
                .id(entity.getId())
                .description(entity.getDescription())
                .amount(entity.getAmount())
                .categoryName(entity.getCategoryName())
                .expenseDate(entity.getExpenseDate())
                .createdAt(entity.getCreatedAt())
                .organizationId(entity.getOrganizationId())
                .empId(entity.getEmpId())
                .employeeName(entity.getEmployeeName())
                .build();
    }
}
