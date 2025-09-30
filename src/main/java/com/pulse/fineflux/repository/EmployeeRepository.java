// src/main/java/com/pulse/fineflux/repository/EmployeeRepository.java
package com.pulse.fineflux.repository;

import com.pulse.fineflux.entity.Employee;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface EmployeeRepository extends MongoRepository<Employee, String> {
    Optional<Employee> findByUsername(String username);
    Optional<Employee> findByEmailId(String emailId);
    boolean existsByUsername(String username);
    boolean existsByEmailId(String emailId);
}
