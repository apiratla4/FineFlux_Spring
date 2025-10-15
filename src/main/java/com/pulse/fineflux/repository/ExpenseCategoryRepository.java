package com.pulse.fineflux.repository;

import com.pulse.fineflux.entity.ExpenseCategory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ExpenseCategoryRepository extends MongoRepository<ExpenseCategory, String> {
    Optional<ExpenseCategory> findByIdAndOrganizationId(String id, String organizationId);
    Optional<ExpenseCategory> findByCategoryNameAndOrganizationId(String categoryName, String organizationId);
    List<ExpenseCategory> findByOrganizationId(String organizationId);
}
