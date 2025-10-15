package com.pulse.fineflux.domain;

import lombok.Data;

@Data
public class ExpenseCategoryUpdateDTO {
    private String categoryName;
    private String organizationId;
}
