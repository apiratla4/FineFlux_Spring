package com.pulse.fineflux.domain;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InventoryLogCreateDTO {
    private String productId;
    private String productName; // new field
    private BigDecimal quantity;
    private BigDecimal currentLevel;
    private String metric;
    private Integer employeeId;
    private String action;
    private BigDecimal previousLevel;// "CREATE" / "UPDATE" etc.
}
