package com.pulse.fineflux.repository;

import com.pulse.fineflux.entity.FinanceSummary;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface FinanceSummaryRepository extends MongoRepository<FinanceSummary, String> {
    List<FinanceSummary> findByOrganizationId(String organizationId);
    FinanceSummary findTopByOrganizationIdOrderByCreatedAtDesc(String organizationId);
    // The field name in the method must match the entity property
    Optional<FinanceSummary> findTopByOrganizationIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            String organizationId, LocalDateTime from, LocalDateTime to
    );

    List<FinanceSummary> findByOrganizationIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            String organizationId, LocalDateTime from, LocalDateTime to
    );
    List<FinanceSummary> findAllByCreatedAtBetweenAndOrganizationId(LocalDateTime start, LocalDateTime end, String organizationId);

}
