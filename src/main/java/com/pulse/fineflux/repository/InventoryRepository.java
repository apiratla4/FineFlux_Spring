package com.pulse.fineflux.repository;

import com.pulse.fineflux.entity.Inventory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface InventoryRepository extends MongoRepository<Inventory, String> {
    List<Inventory> findByProductId(String productId);

    List<Inventory> findAllByProductId(String productId);
}
