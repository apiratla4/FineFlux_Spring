package com.pulse.fineflux.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "expense_category")
public class ExpenseCategory {
    @Id
    private String id;
    private String categoryName;
    private String organizationId;
}
