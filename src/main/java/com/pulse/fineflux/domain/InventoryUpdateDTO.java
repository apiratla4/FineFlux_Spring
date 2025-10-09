package com.pulse.fineflux.domain;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryUpdateDTO {

    private BigDecimal totalCapacity;
    private BigDecimal stockValue;
    private String empId;
    private BigDecimal currentLevel;
    private String metric;
    private Boolean status;
    private BigDecimal tankCapacity;
    private String organizationId;
}
