package com.pulse.fineflux.entity;


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

    private String organizationId;
    private LocalDateTime dateTime;
    private String productName;     // fetched from Product
    private String guns;            // fetched from GunInfo
    private String employeeId;
    private double openingStock;
    private double closingStock;
    private double testingTotal;
    private double salesInLiters;
    private double price;
    private float salesInRupees;
}
