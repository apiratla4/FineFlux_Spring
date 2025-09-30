// src/main/java/com/pulse/fineflux/repository/OrganizationRepository.java
package com.pulse.fineflux.repository;

import com.pulse.fineflux.entity.Organization;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface OrganizationRepository extends MongoRepository<Organization, String> {
    Optional<Organization> findByOrganizationId(String organizationId);
    boolean existsByOrganizationId(String organizationId);
}
