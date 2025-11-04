package com.pulse.fineflux.domain;


import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionsCreateDTO {

    private String organizationId;
    private String empId;
    private LocalDateTime dateTime;
    private double cashReceived;
    private double phonePay;
    private double creditCard;
    private double shortCollections;
    private String productName;   // fetched from Product
    private String guns;
    private double price;
    private double accessCollections;

}
