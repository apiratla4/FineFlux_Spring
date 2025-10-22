// src/main/java/com/pulse/fineflux/service/CustomerHistoryService.java
package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.CustomerHistoryCreateRequest;
import com.pulse.fineflux.domain.CustomerHistoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerHistoryService {
    // POST-only creation; adjusts Customer’s amountBorrowed and totalBorrowedAmount
    CustomerHistoryResponse addTransaction(String organizationId, CustomerHistoryCreateRequest req);

    // Get all history by custId
    Page<CustomerHistoryResponse> listByCustomer(String organizationId, String custId, Pageable pageable);

    // Convenience for latest N
    Page<CustomerHistoryResponse> latestN(String organizationId, String custId, int n);
}
