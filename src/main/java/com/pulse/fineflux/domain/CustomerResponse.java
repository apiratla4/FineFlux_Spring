
package com.pulse.fineflux.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CustomerResponse {
    public String id;
    public String organizationId;
    public String custId;                  // new
    public String customerName;
    public String customerVehicleNum;
    public String empId;
    public BigDecimal amountBorrowed;
    public BigDecimal totalBorrowedAmount;
    public LocalDate borrowDate;
    public LocalDate dueDate;
    public String status;
    public String phoneNumber;
    public String email;
    public String notes;
    public CustomerCreateRequest.AddressDTO address;
}
