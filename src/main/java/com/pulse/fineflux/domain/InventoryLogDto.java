package com.pulse.fineflux.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class InventoryLogDto {
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
