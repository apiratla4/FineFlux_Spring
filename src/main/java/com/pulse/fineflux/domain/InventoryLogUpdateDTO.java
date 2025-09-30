package com.pulse.fineflux.domain;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class InventoryLogUpdateDTO {
    private BigDecimal quantity;
    private BigDecimal currentLevel;
    private String metric;
    private Integer employeeId;
    private String action; // "UPDATE"
}
