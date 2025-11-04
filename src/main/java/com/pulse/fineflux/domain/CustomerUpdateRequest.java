// src/main/java/com/pulse/fineflux/domain/CustomerUpdateRequest.java
package com.pulse.fineflux.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CustomerUpdateRequest {
    public String organizationId;
    public String custId;
    public String customerName;
    public String customerVehicleNum;
    public String empId;
    public BigDecimal amountBorrowed;
    public BigDecimal totalBorrowedAmount;
    public LocalDate borrowDate;
    public LocalDate dueDate;
    public String status;            // BorrowStatus
    public String lifecycleStatus;   // "Active" | "InActive"
    public String phoneNumber;
    public String email;
    public String notes;
    public CustomerCreateRequest.AddressDTO address;
}
