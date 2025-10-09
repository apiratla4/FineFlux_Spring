package com.pulse.fineflux.repository;

import com.pulse.fineflux.entity.Collections;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface CollectionsRepository extends MongoRepository<Collections, String> {
    List<Collections> findByOrganizationId(String organizationId);
    List<Collections> findByOrganizationIdAndDateTime(String organizationId, LocalDateTime dateTime);

}

