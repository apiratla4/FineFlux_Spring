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
        Employee e = repo.findByUsername(req.username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(req.password, e.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        return new LoginResponse(e.getId(), e.getUsername(), e.getRole());
    }
}
