// src/main/java/com/pulse/fineflux/domain/CustomerHistoryResponse.java
package com.pulse.fineflux.domain;

import java.math.BigDecimal;
import java.time.Instant;

public class CustomerHistoryResponse {
    public String id;
    public String organizationId;
    public String customerId;          // Mongo _id
    public String custId;              // business id
    public BigDecimal transactionAmount;
    public BigDecimal cumulativeAmount;
    public Instant transactionDate;
    public String notes;
}
