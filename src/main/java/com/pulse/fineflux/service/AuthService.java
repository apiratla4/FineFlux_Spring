// src/main/java/com/pulse/fineflux/service/AuthService.java
package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.LoginRequest;
import com.pulse.fineflux.domain.LoginResponse;

public interface AuthService {
    LoginResponse authenticate(LoginRequest req);
}
