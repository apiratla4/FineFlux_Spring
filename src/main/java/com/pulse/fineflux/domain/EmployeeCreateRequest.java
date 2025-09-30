// src/main/java/com/pulse/fineflux/domain/employee/EmployeeCreateRequest.java
package com.pulse.fineflux.domain;

import jakarta.validation.constraints.*;

public class EmployeeCreateRequest {

    // Business key distinct from Mongo _id
    @NotBlank
    public String empId;

    // Tenant/business key referencing Organization.organizationId
    @NotBlank
    public String organizationId;

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

    // raw password from client; will be encoded server-side
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
