// src/main/java/com/pulse/fineflux/service/CustomerService.java
package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.CustomerCreateRequest;
import com.pulse.fineflux.domain.CustomerResponse;
import com.pulse.fineflux.domain.CustomerUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface CustomerService {
    CustomerResponse create(String organizationId, CustomerCreateRequest req);
    CustomerResponse get(String organizationId, String id);
    Page<CustomerResponse> list(String organizationId, Pageable pageable);
    CustomerResponse update(String organizationId, String id, CustomerUpdateRequest req);
    void delete(String organizationId, String id);
    long deleteAllForOrganization(String organizationId);

    // NEW: delete by external custId (used by UI)
    void deleteByCustId(String organizationId, String custId);

    // sync helper: update only totalBorrowedAmount by business custId
    CustomerResponse updateTotalBorrowedAmount(String organizationId, String custId, BigDecimal totalBorrowedAmount);
}
