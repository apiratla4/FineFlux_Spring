package com.pulse.fineflux.domain;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GunInfoUpdateDTO {
    private String guns;
    private String serialNumber;
    private Double currentReading;
    private String organizationId;
}
