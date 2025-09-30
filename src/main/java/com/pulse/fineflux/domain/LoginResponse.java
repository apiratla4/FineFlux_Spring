// src/main/java/com/pulse/fineflux/domain/auth/LoginResponse.java
package com.pulse.fineflux.domain;

public class LoginResponse {
    public String id;
    public String username;
    public String role;

    public LoginResponse(String id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }
}
