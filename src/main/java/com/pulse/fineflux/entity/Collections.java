package com.pulse.fineflux.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "collections")
public class Collections {

    @Id
    private String id;

    private String organizationId;
    private LocalDateTime dateTime;
    private String employeeId;

    private double cashReceived;
    private double phonePay;
    private double creditCard;
    private double shortCollections;

    private double expectedTotal;
    private double receivedTotal;
    private double difference;
}
