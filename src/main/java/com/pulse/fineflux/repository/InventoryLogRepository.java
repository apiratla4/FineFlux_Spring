package com.pulse.fineflux.repository;

import com.pulse.fineflux.entity.InventoryLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryLogRepository extends MongoRepository<InventoryLog, String> {

    List<InventoryLog> findByOrganizationId(String organizationId);

    Optional<InventoryLog> findByOrganizationIdAndInventoryId(String organizationId, String inventoryId);

    List<InventoryLog> findByOrganizationIdAndProductNameIgnoreCaseAndLastUpdatedBetween(
            String organizationId, String productName, Date fromDate, Date toDate);


}
