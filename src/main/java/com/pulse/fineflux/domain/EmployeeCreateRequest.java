// src/main/java/com/pulse/fineflux/domain/EmployeeCreateRequest.java
package com.pulse.fineflux.domain;

import jakarta.validation.constraints.*;
import jakarta.validation.constraints.Pattern.Flag;

public class EmployeeCreateRequest {

    @NotBlank
    public String empId;

    @NotBlank
    public String organizationId;

    // ACTIVE or INACTIVE (case-insensitive). If omitted, service defaults to ACTIVE.
    @jakarta.validation.constraints.Pattern(regexp = "ACTIVE|INACTIVE", flags = {Flag.CASE_INSENSITIVE}, message = "status must be ACTIVE or INACTIVE")
    public String status;

    @NotBlank public String role;
    @NotBlank public String department;
    @NotBlank public String firstName;
    @NotBlank public String lastName;

    @Pattern(regexp = "^\\+?[0-9]{7,15}$")
    public String phoneNumber;

    @Email @NotBlank
    public String emailId;

    @NotBlank
    public String username;

    @NotBlank
    public String password;

    public ShiftTimingDTO shiftTiming;
    public AddressDTO address;
    public EmergencyContactDTO emergencyContact;

    public static class ShiftTimingDTO {
        public String start;
        public String end;
    }

    public static class AddressDTO {
        public String line1;
        public String line2;
        public String city;
        public String state;
        public String postalCode;
        public String country;
    }

    public static class EmergencyContactDTO {
        public String name;
        public String phone;
        public String relationship;
    }
}
