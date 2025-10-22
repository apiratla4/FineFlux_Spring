package com.pulse.fineflux.domain;


import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionsUpdateDTO {
    private double cashReceived;
    private double phonePay;
    private double creditCard;
    private double shortCollections;
}
