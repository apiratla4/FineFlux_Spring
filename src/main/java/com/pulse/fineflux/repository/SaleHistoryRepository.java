package com.pulse.fineflux.repository;


import com.pulse.fineflux.entity.SaleHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaleHistoryRepository extends MongoRepository<SaleHistory, String> {

    List<SaleHistory> findByOrganizationIdOrderByDateTimeDesc(String organizationId);


    List<SaleHistory> findByOrganizationIdAndDateTimeBetween(String orgId, LocalDateTime from, LocalDateTime to);

    List<SaleHistory> findByOrganizationIdAndDateTimeBetweenOrderByDateTimeAsc(String organizationId, LocalDateTime from, LocalDateTime to);

    List<SaleHistory> findByOrganizationIdOrderByDateTimeDesc(String organizationId, String productName, String guns);
    List<SaleHistory> findByOrganizationIdOrderByDateTimeAsc(String organizationId);
    Optional<SaleHistory> findByOrganizationIdAndSaleIdAndMutationby(String organizationId,
                                                                     String saleId,
                                                                     String mutationby);
}

