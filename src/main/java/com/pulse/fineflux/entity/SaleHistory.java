package com.pulse.fineflux.entity;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "sale_history")

public class SaleHistory {
    @Id
    private String id;

    private String organizationId;
    private LocalDateTime dateTime;
    private String productName;
    private String guns;
    private String empId;
    private double openingStock;
    private double closingStock;
    private double testingTotal;
    private double salesInLiters;
    private double price;
    private float salesInRupees;
    private double cashReceived;
    private double phonePay;
    private double creditCard;
    private double shortCollections;
    private double receivedTotal;
}
