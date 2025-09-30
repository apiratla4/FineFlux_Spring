package com.pulse.fineflux.repository;

import com.pulse.fineflux.entity.InventoryLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryLogRepository extends MongoRepository<InventoryLog, String> {

    // Get all logs for a product (latest first)
    List<InventoryLog> findByProductIdOrderByTransactionDateDesc(String productId);

    // Get latest log for a product
    Optional<InventoryLog> findTopByProductIdOrderByTransactionDateDesc(String productId);
}
