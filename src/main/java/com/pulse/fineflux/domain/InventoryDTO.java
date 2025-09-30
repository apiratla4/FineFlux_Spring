package com.pulse.fineflux.domain;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDTO {
    private String inventoryId; // auto-generated
    private BigDecimal currentStock;
    private BigDecimal totalCapacity;
    private BigDecimal stockValue;
    private Date lastUpdated;
    private Integer employeeId;
    private String metric;
    private String productId; // reference to Product
    private Boolean status;
    private BigDecimal tankCapacity;
}
