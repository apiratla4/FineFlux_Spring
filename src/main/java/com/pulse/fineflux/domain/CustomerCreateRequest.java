package com.pulse.fineflux.domain;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CustomerCreateRequest {


    @NotBlank
    public String organizationId;

    @NotBlank
    public String customerName;

    @NotBlank
    public String customerVehicleNum;

    @NotBlank
    public String empId;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    public BigDecimal amountBorrowed;

    @PastOrPresent
    public LocalDate borrowDate;

    @FutureOrPresent
    public LocalDate dueDate;

    // enum name: PENDING | PARTIAL | PAID | OVERDUE
    @NotBlank
    public String status;

    @Pattern(regexp = "^\\+?[0-9]{7,15}$")
    public String phoneNumber;

    @Email
    public String email;

    public String notes;

    public AddressDTO address;

    public static class AddressDTO {
        public String line1;
        public String line2;
        public String city;
        public String state;
        public String postalCode;
        public String country;
    }
}
