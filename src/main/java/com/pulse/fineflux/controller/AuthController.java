
package com.pulse.fineflux.controller;

import com.pulse.fineflux.domain.LoginRequest;
import com.pulse.fineflux.domain.LoginResponse;
import com.pulse.fineflux.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return service.authenticate(req);
    }
}
