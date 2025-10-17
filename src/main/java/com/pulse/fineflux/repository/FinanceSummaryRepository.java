package com.pulse.fineflux.repository;

import com.pulse.fineflux.entity.FinanceSummary;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface FinanceSummaryRepository extends MongoRepository<FinanceSummary, String> {
    List<FinanceSummary> findByOrganizationId(String organizationId);
    FinanceSummary findTopByOrganizationIdOrderByCreatedAtDesc(String organizationId);
}
