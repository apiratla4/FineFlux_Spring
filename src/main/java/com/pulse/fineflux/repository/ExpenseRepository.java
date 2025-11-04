package com.pulse.fineflux.repository;


import com.pulse.fineflux.entity.Expense;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends MongoRepository<Expense, String> {
    Optional<Expense> findByIdAndOrganizationId(String id, String organizationId);
    List<Expense> findByOrganizationId(String organizationId);

    // Employee name contains (case-insensitive)
    List<Expense> findByOrganizationIdAndEmployeeNameContainingIgnoreCase(String organizationId, String employeeName);

    // Category name
    List<Expense> findByOrganizationIdAndCategoryName(String organizationId, String categoryName);


    List<Expense> findByOrganizationIdAndExpenseDateBetween(
            String organizationId, LocalDate start, LocalDate end);
}
