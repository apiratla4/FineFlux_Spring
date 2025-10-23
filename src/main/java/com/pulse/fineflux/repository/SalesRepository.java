// src/main/java/com/pulse/fineflux/repository/SalesRepository.java
package com.pulse.fineflux.repository;

import com.pulse.fineflux.entity.Sales;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;

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
     * Find sales by organization, employee, and date range
     */
    List<Sales> findByOrganizationIdAndEmpIdAndDateTimeBetween(
            String organizationId,
            String empId,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    );

    @Query("SELECT s FROM Sales s WHERE s.organizationId = :organizationId " +
            "AND s.empId = :empId " +
            "AND LOWER(s.productName) = LOWER(:productName) " +
            "AND LOWER(s.guns) = LOWER(:guns) " +
            "AND s.dateTime >= :start AND s.dateTime < :end")
    List<Sales> findSalesForCollection(
            @Param("organizationId") String organizationId,
            @Param("empId") String empId,
            @Param("productName") String productName,
            @Param("guns") String guns,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // Case-insensitive search to match any variants like "Petrol", "petrol", etc.
    List<Sales> findByOrganizationIdAndProductNameIgnoreCaseOrderByDateTime(
            String organizationId, String productName
    );
}
