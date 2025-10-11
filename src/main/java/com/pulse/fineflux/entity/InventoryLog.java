package com.pulse.fineflux.entity;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "inventory_logs")
public class InventoryLog {

    @Id
    private String id; // Unique log entry ID—do NOT use inventoryId as your log table's primary key!

    private String inventoryId;
    private String organizationId;
    private String productId;
    private String productName;
    private BigDecimal totalCapacity;
    private BigDecimal stockValue;
    private Date lastUpdated;
    private String empId;
    private BigDecimal currentLevel;
    private String metric;
    private Boolean status;
    private BigDecimal tankCapacity;
}
