
package com.pulse.fineflux.repository;

import com.pulse.fineflux.entity.EmployeeAttendance;
import com.pulse.fineflux.entity.EmployeeDuty;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeDutyRepository extends MongoRepository<EmployeeDuty, String> {

    List<EmployeeDuty> findByOrganizationId(String organizationId);

    List<EmployeeDuty> findByEmpId(String empId);

    List<EmployeeDuty> findByOrganizationIdAndEmpId(String organizationId, String empId);

    Optional<EmployeeDuty> findByOrganizationIdAndEmpIdAndDutyDate(String organizationId, String empId, LocalDate dutyDate);

    //List<EmployeeDuty> findByEmpIdAndDutyDateBetween(String empId, LocalDate startDate, LocalDate endDate);

    List<EmployeeDuty> findByOrganizationIdAndStatus(String organizationId, String status);

    // For current day/period shift lookup
    List<EmployeeDuty> findByEmpIdAndDutyDateBetween(String empId, LocalDate from, LocalDate to);

    // THIS IS THE KEY METHOD for "lookup shift by empId only, most recent"
    EmployeeDuty findTopByEmpIdAndShiftStartNotNullAndShiftEndNotNullOrderByDutyDateDesc(String empId);
    List<EmployeeDuty> findByProductIdsContaining(String productId); // find duties for a given product




}
