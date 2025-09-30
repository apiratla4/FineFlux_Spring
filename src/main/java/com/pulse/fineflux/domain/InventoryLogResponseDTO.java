package com.pulse.fineflux.domain;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
public class InventoryLogResponseDTO {
    private String id;
    private String productId;
    private String productName; // new field
    private BigDecimal quantity;
    private BigDecimal previousLevel;
    private BigDecimal newLevel;
    private BigDecimal currentLevel;
    private String metric;
    private Integer employeeId;
    private Date transactionDate;
    private String action;
}
