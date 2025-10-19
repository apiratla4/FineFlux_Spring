// repository/EmployeeDutyRepository.java
package com.pulse.fineflux.repository;

import com.pulse.fineflux.entity.EmployeeDuty;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeDutyRepository extends MongoRepository<EmployeeDuty, String> {

    List<EmployeeDuty> findByOrganizationId(String organizationId);

    List<EmployeeDuty> findByEmpId(String empId);

    List<EmployeeDuty> findByOrganizationIdAndEmpId(String organizationId, String empId);

    Optional<EmployeeDuty> findByOrganizationIdAndEmpIdAndDutyDate(
            String organizationId, String empId, LocalDate dutyDate);

    List<EmployeeDuty> findByEmpIdAndDutyDateBetween(
            String empId, LocalDate startDate, LocalDate endDate);

    List<EmployeeDuty> findByOrganizationIdAndDutyDateBetween(
            String organizationId, LocalDate startDate, LocalDate endDate);

    List<EmployeeDuty> findByOrganizationIdAndStatus(String organizationId, String status);

    List<EmployeeDuty> findByOrganizationIdAndEmpIdAndDutyDateBetween(
            String organizationId, String empId, LocalDate startDate, LocalDate endDate);

    List<EmployeeDuty> findByOrganizationIdAndDutyDate(String organizationId, LocalDate dutyDate);

    List<EmployeeDuty> findByOrganizationIdAndDutyDateBetweenAndStatus(
            String organizationId, LocalDate startDate, LocalDate endDate, String status);
}
