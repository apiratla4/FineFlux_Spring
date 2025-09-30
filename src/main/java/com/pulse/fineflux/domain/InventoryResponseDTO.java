package com.pulse.fineflux.domain;

import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponseDTO {
    private String inventoryId;
    private BigDecimal currentStock;
    private BigDecimal totalCapacity;
    private BigDecimal stockValue;
    private Date lastUpdated;
    private Integer employeeId;
    private String metric;
    private String productId;
    private String productName;
    private Boolean status;
    private BigDecimal tankCapacity;
}
