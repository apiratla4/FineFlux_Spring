package com.pulse.fineflux.repository;

import com.pulse.fineflux.entity.InventoryLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InventoryLogRepository extends MongoRepository<InventoryLog, String> {
    List<InventoryLog> findByOrganizationId(String organizationId);

    List<InventoryLog> findByOrganizationIdAndProductNameIgnoreCaseOrderByLastUpdatedDesc(String orgId, String productName);

    Optional<InventoryLog> findTopByProductIdOrderByLastUpdatedDesc(String productId);

    @Query("{ 'organizationId': ?0, 'productName': { $regex: ?1, $options: 'i' } }")
    List<InventoryLog> findByOrganizationIdAndProductNameRegex(String orgId, String productNameRegex);

    void deleteById(String id); // Inherited from MongoRepository
    InventoryLog findByInventoryId(String inventoryId);


    // In InventoryLogRepository
    InventoryLog findTopByInventoryIdOrderByLastUpdatedDesc(String inventoryId);

}
