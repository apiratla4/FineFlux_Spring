// src/main/java/com/pulse/fineflux/service/impl/AuthServiceImpl.java
package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.LoginRequest;
import com.pulse.fineflux.domain.LoginResponse;
import com.pulse.fineflux.entity.Employee;
import com.pulse.fineflux.repository.EmployeeRepository;
import com.pulse.fineflux.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthServiceImpl implements AuthService {

    private final EmployeeRepository repo;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(EmployeeRepository repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public LoginResponse authenticate(LoginRequest req) {
        // Look up by username only; orgId will be taken from the employee record
        Employee e = repo.findByUsername(req.username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        // Only allow login for ACTIVE employees
        if (!"ACTIVE".equalsIgnoreCase(e.getStatus())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Employee is inactive. Login not permitted.");
        }


        if (!passwordEncoder.matches(req.password, e.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        // Include organizationId and empId from DB in the response
        return new LoginResponse(
                e.getId(),
                e.getUsername(),
                e.getRole(),
                e.getOrganizationId(),
                e.getEmpId()
        );
    }
}
