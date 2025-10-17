// src/main/java/com/pulse/fineflux/repository/SalesRepository.java
package com.pulse.fineflux.repository;

import com.pulse.fineflux.entity.Sales;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SalesRepository extends MongoRepository<Sales, String> {

    /**
     * Find all sales for an organization
     */
    List<Sales> findByOrganizationId(String organizationId);

    /**
     * Find sales by organization and date range
     * NEW: For analytics date filtering
     */
    List<Sales> findByOrganizationIdAndDateTimeBetween(
            String organizationId,
            LocalDateTime from,
            LocalDateTime to
    );

    /**
     * Find the latest sale for a specific product and gun
     */
    Sales findTopByProductNameAndGunsOrderByDateTimeDesc(String productName, String guns);

    /**
     * Find sales by organization, employee, and date range
     */
    List<Sales> findByOrganizationIdAndEmpIdAndDateTimeBetween(
            String organizationId,
            String empId,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    );

    /**
     * Find sales by organization, employee, product, gun, and date range
     * Useful for detailed filtering
     */
    List<Sales> findByOrganizationIdAndEmpIdAndProductNameAndGunsAndDateTimeBetween(
            String organizationId,
            String empId,
            String productName,
            String guns,
            LocalDateTime from,
            LocalDateTime to
    );

    /**
     * Find sales by exact timestamp match
     * Note: Fragile if milliseconds differ
     */
    List<Sales> findByOrganizationIdAndEmpIdAndProductNameAndGunsAndDateTime(
            String organizationId,
            String empId,
            String productName,
            String guns,
            LocalDateTime dateTime
    );

    /**
     * Find sales by product name and date range
     * Useful for product-specific analytics
     */
    List<Sales> findByOrganizationIdAndProductNameAndDateTimeBetween(
            String organizationId,
            String productName,
            LocalDateTime from,
            LocalDateTime to
    );

    /**
     * Find sales by employee and date range
     * Useful for employee performance tracking
     */
    List<Sales> findByEmpIdAndDateTimeBetween(
            String empId,
            LocalDateTime from,
            LocalDateTime to
    );

    /**
     * Count total sales for an organization
     */
    long countByOrganizationId(String organizationId);

    /**
     * Count sales by organization and date range
     */
    long countByOrganizationIdAndDateTimeBetween(
            String organizationId,
            LocalDateTime from,
            LocalDateTime to
    );
}
