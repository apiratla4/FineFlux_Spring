// src/main/java/com/pulse/fineflux/domain/employee/EmployeeCreateRequest.java
package com.pulse.fineflux.domain;

import jakarta.validation.constraints.*;

public class EmployeeCreateRequest {
    @NotBlank public String role;
    @NotBlank public String department;
    @NotBlank public String firstName;
    @NotBlank public String lastName;
    @Pattern(regexp = "^\\+?[0-9]{7,15}$") public String phoneNumber;
    @Email @NotBlank public String emailId;
    @NotBlank public String username;
    @NotBlank public String password; // raw password from client
    public ShiftTimingDTO shiftTiming;
    public AddressDTO address;
    public EmergencyContactDTO emergencyContact;

    public static class ShiftTimingDTO { public String start; public String end; }
    public static class AddressDTO {
        public String line1, line2, city, state, postalCode, country;
    }
    public static class EmergencyContactDTO {
        public String name, phone, relationship;
    }
}
