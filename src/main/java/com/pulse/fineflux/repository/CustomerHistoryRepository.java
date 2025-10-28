// src/main/java/com/pulse/fineflux/repository/CustomerHistoryRepository.java
package com.pulse.fineflux.repository;

import com.pulse.fineflux.entity.CustomerHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;

public interface CustomerHistoryRepository extends MongoRepository<CustomerHistory, String> {
    Page<CustomerHistory> findByOrganizationIdAndCustIdOrderByTransactionDateDesc(String organizationId, String custId, Pageable pageable);

    Page<CustomerHistory> findByOrganizationIdAndTransactionDateBetweenOrderByTransactionDateDesc(
            String organizationId, Instant from, Instant to, Pageable pageable);

    Page<CustomerHistory> findByOrganizationIdOrderByTransactionDateDesc(
            String organizationId, Pageable pageable);

}
