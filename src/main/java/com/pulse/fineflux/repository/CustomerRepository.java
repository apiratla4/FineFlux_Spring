// src/main/java/com/pulse/fineflux/repository/CustomerRepository.java
package com.pulse.fineflux.repository;

import com.pulse.fineflux.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends MongoRepository<Customer, String> {
    Page<Customer> findAllByOrganizationId(String organizationId, Pageable pageable);
    Optional<Customer> findByIdAndOrganizationId(String id, String organizationId);
    Optional<Customer> findByCustIdAndOrganizationId(String custId, String organizationId);

    // NEW: direct delete by custId + org
    long deleteByCustIdAndOrganizationId(String custId, String organizationId);

    long deleteByOrganizationId(String organizationId);
    List<Customer> findAllByOrganizationIdAndBorrowDateBetween(String orgId, LocalDateTime from, LocalDateTime to);
    List<Customer> findAllByBorrowDateBetweenAndOrganizationId(LocalDate start, LocalDate end, String organizationId);
}
