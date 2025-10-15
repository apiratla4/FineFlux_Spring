package com.pulse.fineflux.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExpenseCategoryResponseDTO {
    private String id;
    private String categoryName;
    private String organizationId;
}
