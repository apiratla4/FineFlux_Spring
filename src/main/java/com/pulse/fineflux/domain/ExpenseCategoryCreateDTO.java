package com.pulse.fineflux.domain;

import lombok.Data;

@Data
public class ExpenseCategoryCreateDTO {
    private String categoryName;
    private String organizationId;
}
