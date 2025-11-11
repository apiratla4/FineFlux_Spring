package com.pulse.fineflux.repository;

import com.pulse.fineflux.entity.InventoryLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InventoryLogRepository extends MongoRepository<InventoryLog, String> {
    List<InventoryLog> findByOrganizationId(String organizationId);


    Optional<InventoryLog> findTopByProductIdOrderByLastUpdatedDesc(String productId);

    @Query("{ 'organizationId': ?0, 'productName': { $regex: ?1, $options: 'i' } }")
    List<InventoryLog> findByOrganizationIdAndProductNameRegex(String orgId, String productNameRegex);

    void deleteById(String id); // Inherited from MongoRepository
    InventoryLog findByInventoryId(String inventoryId);

    // In InventoryLogRepository
    InventoryLog findTopByInventoryIdOrderByLastUpdatedDesc(String inventoryId);

    InventoryLog findTopByProductIdAndInventoryIdNotOrderByLastUpdatedDesc(String productId, String inventoryId);

    Optional<InventoryLog> findByInventoryIdAndMutationby(String inventoryId, String mutationby);

    // New: find the latest InventoryLog for a product that occurred before the given timestamp
    Optional<InventoryLog> findTopByProductIdAndLastUpdatedLessThanOrderByLastUpdatedDesc(String productId, LocalDateTime time);

    // New: Fetch all logs for a product ordered by lastUpdated descending (for custom selection)
    List<InventoryLog> findByProductIdOrderByLastUpdatedDesc(String productId);
}
