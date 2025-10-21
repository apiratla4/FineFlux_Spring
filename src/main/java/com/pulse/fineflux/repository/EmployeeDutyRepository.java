package com.pulse.fineflux.repository;

import com.pulse.fineflux.entity.EmployeeDuty;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeDutyRepository extends MongoRepository<EmployeeDuty, String> {

    List<EmployeeDuty> findByOrgId(String orgId);

    List<EmployeeDuty> findByEmpId(String empId);

    List<EmployeeDuty> findByOrgIdAndEmpId(String orgId, String empId);

    Optional<EmployeeDuty> findByOrgIdAndEmpIdAndDutyDate(String orgId, String empId, LocalDate dutyDate);

    List<EmployeeDuty> findByEmpIdAndDutyDateBetween(String empId, LocalDate startDate, LocalDate endDate);

    List<EmployeeDuty> findByOrgIdAndStatus(String orgId, String status);

    // New methods for date-based filtering
    List<EmployeeDuty> findByOrgIdAndDutyDate(String orgId, LocalDate dutyDate);

    List<EmployeeDuty> findByOrgIdAndDutyDateBetween(String orgId, LocalDate startDate, LocalDate endDate);
}
