package com.pulse.fineflux.entity;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.util.Date;


@Document(collection = "inventory")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    @Id
    private String inventoryId; // MongoDB _id

    private String organizationId; // 🔹 added for multi-tenancy

    private String productId;      // reference to Product
    private String productName;
    private BigDecimal totalCapacity;
    private BigDecimal stockValue;
    private Date lastUpdated;
    private BigDecimal currentLevel;
    private String metric;
    private Boolean status;
    private BigDecimal tankCapacity;
    private String empId;

}
