package com.pulse.fineflux.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "sales")
public class Sales {

    @Id
    private String id;

    // Business reference to link Collection and History
    private String saleId; // UUID string
    private String organizationId;
    private LocalDateTime dateTime;
    private String productName;   // fetched from Product
    private String guns;          // fetched from GunInfo
    private String empId;
    private double openingStock;
    private double closingStock;
    private double testingTotal;
    private double salesInLiters;
    private double price;
    private float salesInRupees;

    // Derived key at second resolution for robust match
    private String saleMatchKey;

}
