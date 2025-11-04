package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.*;
import com.pulse.fineflux.entity.ExpenseCategory;
import com.pulse.fineflux.repository.ExpenseCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseCategoryServiceImpl implements ExpenseCategoryService {

    private final ExpenseCategoryRepository repo;

    @Override
    public ExpenseCategoryResponseDTO create(ExpenseCategoryCreateDTO dto) {
        try {
            if(repo.findByCategoryNameAndOrganizationId(dto.getCategoryName(), dto.getOrganizationId()).isPresent())
                throw new RuntimeException("Category already exists for organization");
            ExpenseCategory entity = ExpenseCategory.builder()
                    .categoryName(dto.getCategoryName())
                    .organizationId(dto.getOrganizationId())
                    .build();
            ExpenseCategory saved = repo.save(entity);
            log.info("ExpenseCategory created: {}", saved);
            return toResponse(saved);
        } catch(Exception e) {
            log.error("Error creating category: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public ExpenseCategoryResponseDTO update(String id, ExpenseCategoryUpdateDTO dto) {
        try {
            ExpenseCategory entity = repo.findByIdAndOrganizationId(id, dto.getOrganizationId())
                    .orElseThrow(() -> new RuntimeException("Category not found (org)"));
            entity.setCategoryName(dto.getCategoryName());
            ExpenseCategory saved = repo.save(entity);
            log.info("ExpenseCategory updated: {}", saved);
            return toResponse(saved);
        } catch(Exception e) {
            log.error("Error updating category: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void deleteByOrg(String id, String organizationId) {
        try {
            ExpenseCategory entity = repo.findByIdAndOrganizationId(id, organizationId)
                    .orElseThrow(() -> new RuntimeException("Category not found (org)"));
            repo.deleteById(entity.getId());
            log.info("ExpenseCategory deleted: {}", id);
        } catch(Exception e) {
            log.error("Error deleting category: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public ExpenseCategoryResponseDTO getByOrgAndId(String organizationId, String id) {
        return repo.findByIdAndOrganizationId(id, organizationId)
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Category not found (org)"));
    }

    @Override
    public List<ExpenseCategoryResponseDTO> getAllByOrg(String organizationId) {
        return repo.findByOrganizationId(organizationId).stream()
                .map(this::toResponse).toList();
    }

    private ExpenseCategoryResponseDTO toResponse(ExpenseCategory entity) {
        return ExpenseCategoryResponseDTO.builder()
                .id(entity.getId())
                .categoryName(entity.getCategoryName())
                .organizationId(entity.getOrganizationId())
                .build();
    }
}
