// src/main/java/com/pulse/fineflux/repository/DocumentRepository.java
package com.pulse.fineflux.repository;

import com.pulse.fineflux.entity.DocumentRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface DocumentRepository extends MongoRepository<DocumentRecord, String> {
    Page<DocumentRecord> findAllByOrganizationId(String organizationId, Pageable pageable);
    Optional<DocumentRecord> findByIdAndOrganizationId(String id, String organizationId);
    boolean existsByIdAndOrganizationId(String id, String organizationId);
}
