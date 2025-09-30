package com.pulse.fineflux.domain;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryUpdateDTO {
    private BigDecimal currentStock;
    private BigDecimal totalCapacity;
    private BigDecimal stockValue;
    private Integer employeeId;
    private String metric;
    private Boolean status;
    private BigDecimal tankCapacity;
}
