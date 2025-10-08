
package com.pulse.fineflux.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CustomerUpdateRequest {
    public String organizationId;       // must match current org; change not allowed
    public String custId;               // business id; change allowed if desired
    public String customerName;
    public String customerVehicleNum;
    public String empId;
    public BigDecimal amountBorrowed;   // current outstanding (not recommended to patch directly)
    public BigDecimal totalBorrowedAmount; // optional if manually syncing
    public LocalDate borrowDate;
    public LocalDate dueDate;
    public String status;
    public String phoneNumber;
    public String email;
    public String notes;
    public CustomerCreateRequest.AddressDTO address;
}
