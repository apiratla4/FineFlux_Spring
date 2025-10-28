// src/main/java/com/pulse/fineflux/domain/CustomerHistoryCreateRequest.java
package com.pulse.fineflux.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

public class CustomerHistoryCreateRequest {
    @NotBlank public String custId;
     // business id from UI
    @NotNull  public BigDecimal transactionAmount;  // +borrow, -payment
    public Instant transactionDate;
    public String notes;
}
