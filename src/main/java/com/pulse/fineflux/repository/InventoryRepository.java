package com.pulse.fineflux.repository;

import com.pulse.fineflux.entity.Inventory;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends MongoRepository<Inventory, String> {
    List<Inventory> findAllByOrganizationId(String organizationId);

    List<Inventory> findAllByOrganizationIdAndProductId(String orgId, String productId);
    // Fetch a single latest inventory entry for an org/product via timestamp
    Inventory findTopByOrganizationIdAndProductIdOrderByLastUpdatedDesc(String orgId, String productId);

    List<Inventory> findByProductId(String productId);



}
