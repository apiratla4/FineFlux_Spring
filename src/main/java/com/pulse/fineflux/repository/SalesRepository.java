package com.pulse.fineflux.repository;


import com.pulse.fineflux.entity.Employee;
import com.pulse.fineflux.entity.Sales;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SalesRepository extends MongoRepository<Sales, String> {
    List<Sales> findByOrganizationId(String organizationId);
    Sales findTopByProductNameAndGunsOrderByDateTimeDesc(String productName, String guns);

    List<Sales> findByOrganizationIdAndEmpIdAndDateTimeBetween(
            String organizationId,
            String empId,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    );
    List<Sales> findByOrganizationIdAndEmpIdAndProductNameAndGunsAndDateTime(
            String organizationId, String empId, String productName, String guns, LocalDateTime dateTime);


}
