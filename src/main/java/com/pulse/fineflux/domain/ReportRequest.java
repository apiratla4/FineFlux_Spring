package com.pulse.fineflux.domain;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Request model for all reporting operations.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReportRequest {
    private String entityName;   // SALES, INVENTORY, CUSTOMER, EMPLOYEE, FINANCESUMMARY
    private String reportType;   // DAY, MONTH, CUSTOM
    private LocalDate day;       // For day-wise report
    private Integer year;        // For month-wise report
    private Integer month;       // For month-wise report
    private LocalDateTime from;  // For custom range
    private LocalDateTime to;    // For custom range
    private String organizationId;
}
