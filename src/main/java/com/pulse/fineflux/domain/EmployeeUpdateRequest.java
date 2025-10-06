// src/main/java/com/pulse/fineflux/domain/EmployeeUpdateRequest.java
package com.pulse.fineflux.domain;

import jakarta.validation.constraints.*;
import jakarta.validation.constraints.Pattern.Flag;

public class EmployeeUpdateRequest {
    public String empId;
    public String organizationId;

    // Allow toggling status
    @jakarta.validation.constraints.Pattern(regexp = "ACTIVE|INACTIVE", flags = {Flag.CASE_INSENSITIVE}, message = "status must be ACTIVE or INACTIVE")
    public String status;

    public String role;
    public String department;
    public String firstName;
    public String lastName;

    @Pattern(regexp = "^\\+?[0-9]{7,15}$")
    public String phoneNumber;

    @Email
    public String emailId;

    public String username;
    public String newPassword;

    public EmployeeCreateRequest.ShiftTimingDTO shiftTiming;
    public EmployeeCreateRequest.AddressDTO address;
    public EmployeeCreateRequest.EmergencyContactDTO emergencyContact;
}
