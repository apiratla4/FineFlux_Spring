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
    private String inventoryId; // MongoDB _id

    private String organizationId; // 🔹 added for multi-tenancy

    private String productId;      // reference to Product
    private String productName;
    private BigDecimal totalCapacity;
    private BigDecimal stockValue;
    private Date lastUpdated;
    private Integer employeeId;
    private BigDecimal currentLevel;
    private String metric;
    private Boolean status;
    private BigDecimal tankCapacity;

}
