package com.pulse.fineflux.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "guninfo")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GunInfo {

    @Id
    private String id;

    private String organizationId;
    private String guns;
    private String serialNumber;
    private double currentReading;
}
