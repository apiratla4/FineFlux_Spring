package com.pulse.fineflux.entity;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document(collection = "product")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    private String productId; // primary key in MongoDB
    private String productName;
    private Double price;
    private Boolean status;
    private BigDecimal tankCapacity;
    private String description;
    private String supplier;
    private BigDecimal currentLevel;
    private String metric;
}
