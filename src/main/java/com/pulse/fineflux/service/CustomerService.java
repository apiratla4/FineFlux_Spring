package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.CustomerCreateRequest;
import com.pulse.fineflux.domain.CustomerResponse;
import com.pulse.fineflux.domain.CustomerUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface CustomerService {
    CustomerResponse create(String organizationId, CustomerCreateRequest req);
    CustomerResponse get(String organizationId, String id);
    Page<CustomerResponse> list(String organizationId, Pageable pageable);
    CustomerResponse update(String organizationId, String id, CustomerUpdateRequest req);
    void delete(String organizationId, String id);
    long deleteAllForOrganization(String organizationId);
    void deleteByCustId(String organizationId, String custId);
    CustomerResponse updateTotalBorrowedAmount(String organizationId, String custId, BigDecimal totalBorrowedAmount);

    // ✅ NEW: Date-based filters
    Page<CustomerResponse> listByDate(String organizationId, LocalDate date, Pageable pageable);
    Page<CustomerResponse> listByDateRange(String organizationId, LocalDate startDate, LocalDate endDate, Pageable pageable);
    Page<CustomerResponse> listToday(String organizationId, Pageable pageable);
    Page<CustomerResponse> listThisWeek(String organizationId, Pageable pageable);
    Page<CustomerResponse> listThisMonth(String organizationId, Pageable pageable);
}
