package com.pulse.fineflux.domain;

import lombok.*;
import java.util.List;

/**
 * Generic response model for all reports.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReportResponse<T> {
    private String entityName;
    private String reportType;
    private String fromDate;
    private String toDate;
    private Integer totalRecords;
    private Object summary; // Holds special summary DTO depending on entity/report
    private List<T> data;
}
