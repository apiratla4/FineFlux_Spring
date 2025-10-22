// src/main/java/com/pulse/fineflux/service/impl/CustomerHistoryServiceImpl.java
package com.pulse.fineflux.service;
import com.pulse.fineflux.domain.CustomerHistoryCreateRequest;
import com.pulse.fineflux.domain.CustomerHistoryResponse;
import com.pulse.fineflux.entity.Customer;
import com.pulse.fineflux.entity.CustomerHistory;
import com.pulse.fineflux.repository.CustomerHistoryRepository;
import com.pulse.fineflux.repository.CustomerRepository;
import com.pulse.fineflux.service.CustomerHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Slf4j
@Service
public class CustomerHistoryServiceImpl implements CustomerHistoryService {

    private final CustomerHistoryRepository historyRepo;
    private final CustomerRepository customerRepo;

    public CustomerHistoryServiceImpl(CustomerHistoryRepository historyRepo, CustomerRepository customerRepo) {
        this.historyRepo = historyRepo;
        this.customerRepo = customerRepo;
    }

    @Override
    public CustomerHistoryResponse addTransaction(String organizationId, CustomerHistoryCreateRequest req) {
        log.info("Adding customer history txn orgId={} custId={} amt={}", organizationId, req.custId, req.transactionAmount);

        Customer cust = customerRepo.findByCustIdAndOrganizationId(req.custId, organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        if (req.transactionAmount == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "transactionAmount required");
        }

        // Detect opening balance history row -> do NOT mutate customer totals, just record a mirror entry
        final String OPENING_NOTE = "Opening balance on customer creation";
        boolean isOpening = req.notes != null && req.notes.equalsIgnoreCase(OPENING_NOTE);

        CustomerHistory h = new CustomerHistory();
        h.setOrganizationId(organizationId);
        h.setCustomerId(cust.getId());
        h.setCustId(cust.getCustId());
        h.setTransactionAmount(req.transactionAmount);
        h.setTransactionDate(req.transactionDate != null ? req.transactionDate : Instant.now());
        h.setNotes(req.notes);

        // Null-safe current values
        BigDecimal currentDebt = cust.getAmountBorrowed() != null ? cust.getAmountBorrowed() : BigDecimal.ZERO;
        BigDecimal currentTotalBorrowed = cust.getTotalBorrowedAmount() != null ? cust.getTotalBorrowedAmount() : BigDecimal.ZERO;

        if (isOpening) {
            // Only mirror the current customer state; do not re-add to totals
            h.setCumulativeAmount(currentDebt);
            h = historyRepo.save(h);
            return toResponse(h);
        }

        // Sign convention:
        //   - Positive amount = Payment received => reduces outstanding debt
        //   - Negative amount = Extra borrowed => increases outstanding debt
        BigDecimal txn = req.transactionAmount;

        // Apply: newDebt = currentDebt - txn
        //   payment: current - (+pmt) => decreases
        //   extra:   current - (-borrow) => increases
        BigDecimal newDebt = currentDebt.subtract(txn);

        // Do not allow negative outstanding
        if (newDebt.signum() < 0) {
            newDebt = BigDecimal.ZERO;
        }

        // Update customer's outstanding
        cust.setAmountBorrowed(newDebt);

        // Update total borrowed only when extra borrowed occurs (txn < 0), add absolute value
        if (txn.signum() < 0) {
            currentTotalBorrowed = currentTotalBorrowed.add(txn.abs());
            cust.setTotalBorrowedAmount(currentTotalBorrowed);
        }

        // Status adjustments
        LocalDate today = LocalDate.now();
        if (cust.getDueDate() != null && cust.getDueDate().isBefore(today) && newDebt.signum() > 0) {
            cust.setStatus(Customer.BorrowStatus.OVERDUE);
        } else if (newDebt.signum() == 0) {
            cust.setStatus(Customer.BorrowStatus.PAID);
        } else {
            cust.setStatus(Customer.BorrowStatus.PARTIAL);
        }

        customerRepo.save(cust);

        // Persist history with running balance
        h.setCumulativeAmount(newDebt);
        h = historyRepo.save(h);
        return toResponse(h);
    }

    @Override
    public Page<CustomerHistoryResponse> listByCustomer(String organizationId, String custId, Pageable pageable) {
        return historyRepo.findByOrganizationIdAndCustIdOrderByTransactionDateDesc(organizationId, custId, pageable)
                .map(this::toResponse);
    }

    @Override
    public Page<CustomerHistoryResponse> latestN(String organizationId, String custId, int n) {
        return listByCustomer(organizationId, custId, PageRequest.of(0, n));
    }

    private CustomerHistoryResponse toResponse(CustomerHistory h) {
        CustomerHistoryResponse r = new CustomerHistoryResponse();
        r.id = h.getId();
        r.organizationId = h.getOrganizationId();
        r.customerId = h.getCustomerId();
        r.custId = h.getCustId();
        r.transactionAmount = h.getTransactionAmount();
        r.cumulativeAmount = h.getCumulativeAmount();
        r.transactionDate = h.getTransactionDate();
        r.notes = h.getNotes();
        return r;
    }
}
