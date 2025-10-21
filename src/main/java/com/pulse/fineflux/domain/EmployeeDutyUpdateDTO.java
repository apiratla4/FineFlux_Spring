package com.pulse.fineflux.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDutyUpdateDTO {

    private LocalDate dutyDate;
    private List<String> products;
    private List<String> guns;
    private String shiftStart;
    private String shiftEnd;
    private String status;
}
