
package com.pulse.fineflux.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Document(collection = "customerHistories")
public class CustomerHistory {

    @Id
    private String id;

    private String organizationId;
    private String customerId;   // Mongo _id of Customer for referential link
    private String custId;       // Business customer ID

    // Positive for borrow, negative for payment
    private BigDecimal transactionAmount;

    // Customer’s outstanding after this transaction
    private BigDecimal cumulativeAmount;

    private Instant transactionDate;

    private String notes;
}
