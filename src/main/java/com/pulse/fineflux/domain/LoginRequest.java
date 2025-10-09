// src/main/java/com/pulse/fineflux/domain/LoginRequest.java
package com.pulse.fineflux.domain;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    @NotBlank
    public String username;

    @NotBlank
    public String password;
}
