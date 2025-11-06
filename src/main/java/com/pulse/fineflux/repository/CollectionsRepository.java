package com.pulse.fineflux.repository;

import com.pulse.fineflux.entity.Collections;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CollectionsRepository extends MongoRepository<Collections, String> {
    List<Collections> findByOrganizationId(String organizationId);

    List<Collections> findByOrganizationIdAndDateTimeBetween(String organizationId, LocalDateTime from, LocalDateTime to);

    List<Collections> findAllByOrganizationIdAndProductNameAndGuns(String organizationId, String productName, String guns);
    Long deleteByOrganizationIdAndSaleId(String organizationId, String saleId);
    Optional<Collections> findBySaleId(String saleId);

}

