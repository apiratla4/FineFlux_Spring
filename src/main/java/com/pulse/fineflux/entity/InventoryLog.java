package com.pulse.fineflux.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.Date;

@Document(collection = "inventory_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryLog {

    @Id
    private String id;

    private String productId;

    private BigDecimal quantity;
    private BigDecimal previousLevel; // Add this
    private BigDecimal newLevel;      // Add this
    private BigDecimal currentLevel;

    private String metric;
    private Integer employeeId;
    private Date transactionDate;
    private String action;
}
