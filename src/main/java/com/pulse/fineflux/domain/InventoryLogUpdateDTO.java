package com.pulse.fineflux.domain;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class InventoryLogUpdateDTO {
    private BigDecimal totalCapacity;
    private BigDecimal stockValue;
    private String empId;
    private BigDecimal currentLevel;
    private String metric;
    private Boolean status;
    private BigDecimal tankCapacity;
}
