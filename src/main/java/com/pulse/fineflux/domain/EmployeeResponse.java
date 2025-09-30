// src/main/java/com/pulse/fineflux/domain/employee/EmployeeResponse.java
package com.pulse.fineflux.domain;

import java.time.Instant;

public class EmployeeResponse {
    public String id;               // Mongo _id
    public String empId;            // business employee id
    public String organizationId;   // business org id

    public String role;
    public String department;
    public String firstName;
    public String lastName;
    public String phoneNumber;
    public String emailId;
    public String username;

    public Instant joinedDate;

    public EmployeeCreateRequest.ShiftTimingDTO shiftTiming;
    public EmployeeCreateRequest.AddressDTO address;
    public EmployeeCreateRequest.EmergencyContactDTO emergencyContact;
}
