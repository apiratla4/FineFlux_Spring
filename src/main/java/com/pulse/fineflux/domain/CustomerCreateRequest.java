// src/main/java/com/pulse/fineflux/domain/customer/CustomerCreateRequest.java
package com.pulse.fineflux.domain;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CustomerCreateRequest {
    @NotBlank public String customerName;
    @NotBlank public String customerVehicleNum;
    @NotBlank public String empId;
    @NotNull @DecimalMin(value = "0.0", inclusive = false) public BigDecimal amountBorrowed;
    @PastOrPresent public LocalDate borrowDate;
    @FutureOrPresent public LocalDate dueDate;
    @NotBlank public String status; // enum name: PENDING | PARTIAL | PAID | OVERDUE
    @Pattern(regexp = "^\\+?[0-9]{7,15}$") public String phoneNumber;
    @Email public String email;
    public String notes;
    public AddressDTO address;

    public static class AddressDTO {
        public String line1, line2, city, state, postalCode, country;
    }
}
