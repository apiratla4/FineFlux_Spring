package com.pulse.fineflux.repository;

import com.pulse.fineflux.entity.InventoryLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface InventoryLogRepository extends MongoRepository<InventoryLog, String> {

    // Find all logs for a specific product ordered by transaction date descending
    List<InventoryLog> findByProductIdOrderByTransactionDateDesc(String productId);

    // Search logs by product name (partial match)
    List<InventoryLog> findByProductNameContainingIgnoreCase(String productName);

    // Search logs by transaction date range
    List<InventoryLog> findByTransactionDateBetween(Date from, Date to);

    // Search logs by product name and transaction date range
    List<InventoryLog> findByProductNameContainingIgnoreCaseAndTransactionDateBetween(
            String productName, Date from, Date to
    );
}
