package com.pulse.fineflux.domain;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GunInfoResponseDTO {
    private String id;
    private String organizationId;
    private String guns;
    private String serialNumber;
    private Double currentReading;
    private String productName;
    private String empId;
}
