package com.pulse.fineflux.repository;

import com.pulse.fineflux.entity.CustomerHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface CustomerHistoryRepository extends MongoRepository<CustomerHistory, String> {
    Page<CustomerHistory> findByOrganizationIdAndCustIdOrderByTransactionDateDesc(String organizationId, String custId, Pageable pageable);

    // ✅ CRITICAL: This method must exist for cascade delete
    long deleteByOrganizationIdAndCustId(String organizationId, String custId);

    // Optional: For debugging
    long countByOrganizationIdAndCustId(String organizationId, String custId);
}
