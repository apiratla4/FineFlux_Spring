package com.pulse.fineflux.repository;


import com.pulse.fineflux.entity.SaleHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaleHistoryRepository extends MongoRepository<SaleHistory, String> {

    List<SaleHistory> findByOrganizationId(String orgId);

    List<SaleHistory> findByOrganizationIdAndDateTimeBetween(String orgId, LocalDateTime from, LocalDateTime to);
}

