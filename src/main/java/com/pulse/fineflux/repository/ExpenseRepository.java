package com.pulse.fineflux.repository;


import com.pulse.fineflux.entity.Expense;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends MongoRepository<Expense, String> {
    Optional<Expense> findByIdAndOrganizationId(String id, String organizationId);
    List<Expense> findByOrganizationId(String organizationId);
}
