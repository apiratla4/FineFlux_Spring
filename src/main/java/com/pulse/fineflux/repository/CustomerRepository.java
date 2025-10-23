package com.pulse.fineflux.repository;

import com.pulse.fineflux.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDate;
import java.util.Optional;

public interface CustomerRepository extends MongoRepository<Customer, String> {
    Page<Customer> findAllByOrganizationId(String organizationId, Pageable pageable);
    Optional<Customer> findByIdAndOrganizationId(String id, String organizationId);
    Optional<Customer> findByCustIdAndOrganizationId(String custId, String organizationId);

    long deleteByCustIdAndOrganizationId(String custId, String organizationId);
    long deleteByOrganizationId(String organizationId);

    // ✅ NEW: Date-based filters
    Page<Customer> findByOrganizationIdAndBorrowDate(String organizationId, LocalDate borrowDate, Pageable pageable);

    Page<Customer> findByOrganizationIdAndBorrowDateBetween(
            String organizationId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );

    // Alternative: Query by status
    Page<Customer> findByOrganizationIdAndStatus(
            String organizationId,
            Customer.BorrowStatus status,
            Pageable pageable
    );
}
