
package com.pulse.fineflux.domain;

import jakarta.validation.constraints.*;

public class EmployeeUpdateRequest {
    public String role;
    public String department;
    public String firstName;
    public String lastName;
    @Pattern(regexp = "^\\+?[0-9]{7,15}$") public String phoneNumber;
    @Email public String emailId;
    public String username;
    public String newPassword; // optional
    public EmployeeCreateRequest.ShiftTimingDTO shiftTiming;
    public EmployeeCreateRequest.AddressDTO address;
    public EmployeeCreateRequest.EmergencyContactDTO emergencyContact;
}
