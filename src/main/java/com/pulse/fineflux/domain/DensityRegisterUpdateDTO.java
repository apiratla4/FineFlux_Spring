package com.pulse.fineflux.domain;

import lombok.*;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DensityRegisterUpdateDTO {
    private LocalDateTime dateTime;
    private double hydramMeter;
    private double tempaturInCelsius;
    private Map<String, Double> receiptQuantityInLitres; // tank1..tank4
    private String ttRegnNo;
    private double compositeReading;
    private double asPerChallan;
    private Map<String, Double> densityAt15AfterDecantation; // tank1..tank4
    private String productName;
}
