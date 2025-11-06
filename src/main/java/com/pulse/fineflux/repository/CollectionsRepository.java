package com.pulse.fineflux.repository;

import com.pulse.fineflux.entity.Collections;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface CollectionsRepository extends MongoRepository<Collections, String> {
    List<Collections> findByOrganizationId(String organizationId);

    List<Collections> findByOrganizationIdAndEmpIdAndDateTimeBetween(String orgId, String empId, LocalDateTime from, LocalDateTime to);

    List<Collections> findByOrganizationIdAndDateTimeBetween(String organizationId, LocalDateTime from, LocalDateTime to);

    List<Collections> findAllByOrganizationIdAndProductNameAndGuns(String organizationId, String productName, String guns);
    Long deleteByOrganizationIdAndSaleId(String organizationId, String saleId);

}

