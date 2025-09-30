// src/main/java/com/pulse/fineflux/domain/employee/EmployeeUpdateRequest.java
package com.pulse.fineflux.domain;

import jakarta.validation.constraints.*;

public class EmployeeUpdateRequest {

    // Optional updates to business keys; enforce policy in service layer if disallowed
    public String empId;
    public String organizationId;

    public String role;
    public String department;
    public String firstName;
    public String lastName;

    @Pattern(regexp = "^\\+?[0-9]{7,15}$")
    public String phoneNumber;

    @Email
    public String emailId;

    public String username;

    // optional new password to re-encode and store
    public String newPassword;

    public EmployeeCreateRequest.ShiftTimingDTO shiftTiming;
    public EmployeeCreateRequest.AddressDTO address;
    public EmployeeCreateRequest.EmergencyContactDTO emergencyContact;
}
