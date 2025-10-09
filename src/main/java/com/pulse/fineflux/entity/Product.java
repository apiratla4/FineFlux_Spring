
package com.pulse.fineflux.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "products")
public class Product {
    @Id
    private String id; // Mongo _id

    @NotBlank
    private String organizationId; // business key

    @NotBlank
    private String productName;

    private Double price;
    private Boolean status;
    private BigDecimal tankCapacity;
    private String description;
    private String supplier;
    private BigDecimal currentLevel;
    private String metric;
}
