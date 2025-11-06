package com.pulse.fineflux.domain;


import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class SaleHistoryResponseDTO {
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
    private String mutationby;
    private String dateTimeString;
    private String uuid;
}
