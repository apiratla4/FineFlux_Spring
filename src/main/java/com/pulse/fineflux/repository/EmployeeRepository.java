// src/main/java/com/pulse/fineflux/repository/EmployeeRepository.java
package com.pulse.fineflux.repository;

import com.pulse.fineflux.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface EmployeeRepository extends MongoRepository<Employee, String> {
    // Global lookups (keep for login/admin tools)
    Optional<Employee> findByUsername(String username);
    Optional<Employee> findByEmailId(String emailId);
    boolean existsByUsername(String username);              // use only if username is globally unique
    boolean existsByEmailId(String emailId);                // email is globally unique

    // Business key checks
    boolean existsByEmpId(String empId);                    // global unique empId (as per entity index)

    // Org-scoped operations
    Page<Employee> findAllByOrganizationId(String organizationId, Pageable pageable);
    Optional<Employee> findByIdAndOrganizationId(String id, String organizationId);
    boolean existsByIdAndOrganizationId(String id, String organizationId);

    // Per-org username uniqueness (use if username is unique per org)
    boolean existsByOrganizationIdAndUsername(String organizationId, String username);
}
