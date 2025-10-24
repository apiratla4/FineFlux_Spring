package com.pulse.fineflux.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "densityregister")
public class DensityRegister {
    @Id
    private String id;
    private LocalDateTime dateTime;
    private double hydramMeter;
    private double tempaturInCelsius;
    private double convertedToCelsius;
    private Map<String, Double> receiptQuantityInLitres; // tank1..tank4
    private String ttRegnNo;
    private double compositeReading;
    private double asPerChallan;
    private double difference;
    private Map<String, Double> densityAt15AfterDecantation; // tank1..tank4
    private String productName;
    private String organizationId;
}
