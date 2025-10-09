// src/main/java/com/pulse/fineflux/repository/CustomerRepository.java
package com.pulse.fineflux.repository;

import com.pulse.fineflux.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CustomerRepository extends MongoRepository<Customer, String> {
    Page<Customer> findAllByOrganizationId(String organizationId, Pageable pageable);
    Optional<Customer> findByIdAndOrganizationId(String id, String organizationId);
    Optional<Customer> findByCustIdAndOrganizationId(String custId, String organizationId);
    long deleteByOrganizationId(String organizationId);
}
