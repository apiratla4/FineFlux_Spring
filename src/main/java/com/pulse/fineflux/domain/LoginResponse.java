// src/main/java/com/pulse/fineflux/domain/LoginResponse.java
package com.pulse.fineflux.domain;

public class LoginResponse {
    public String id;               // employee Mongo _id
    public String username;
    public String role;
    public String organizationId;   // fetched from DB
    public String empId;            // business employee id (from DB)

    public LoginResponse(String id, String username, String role, String organizationId, String empId) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.organizationId = organizationId;
        this.empId = empId;
    }
}
