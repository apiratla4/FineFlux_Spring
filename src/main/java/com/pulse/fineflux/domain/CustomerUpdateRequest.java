package com.pulse.fineflux.domain;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CustomerUpdateRequest {
    public String organizationId; // optional change of tenant association if allowed
    public String customerName;
    public String customerVehicleNum;
    public String empId;
    public BigDecimal amountBorrowed;
    public LocalDate borrowDate;
    public LocalDate dueDate;
    public String status; // enum name
    public String phoneNumber;
    public String email;
    public String notes;
    public CustomerCreateRequest.AddressDTO address;
}
