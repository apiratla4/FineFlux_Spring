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
import java.time.ZoneId;
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
        try {
            Customer cust = customerRepo.findByCustIdAndOrganizationId(req.custId, organizationId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

            if (req.transactionAmount == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "transactionAmount required");
            }

            final String OPENING_NOTE = "Opening balance on customer creation";
            boolean isOpening = req.notes != null && req.notes.equalsIgnoreCase(OPENING_NOTE);

            CustomerHistory h = new CustomerHistory();
            h.setOrganizationId(organizationId);
            h.setCustomerId(cust.getId());
            h.setCustId(cust.getCustId());
            h.setTransactionAmount(req.transactionAmount);
            h.setTransactionDate(req.transactionDate != null ? req.transactionDate : com.pulse.fineflux.utill.DateTimeUtil.nowInstant());
            h.setNotes(req.notes);

            BigDecimal currentDebt = cust.getAmountBorrowed() != null ? cust.getAmountBorrowed() : BigDecimal.ZERO;
            BigDecimal currentTotalBorrowed = cust.getTotalBorrowedAmount() != null ? cust.getTotalBorrowedAmount() : BigDecimal.ZERO;

            if (isOpening) {
                h.setCumulativeAmount(currentDebt);
                h = historyRepo.save(h);
                log.info("Opening balance entry saved for custId={}, orgId={}", req.custId, organizationId);
                return toResponse(h);
            }

            BigDecimal txn = req.transactionAmount;
            BigDecimal newDebt = currentDebt.subtract(txn);

            if (newDebt.signum() < 0) {
                newDebt = BigDecimal.ZERO;
            }

            cust.setAmountBorrowed(newDebt);

            if (txn.signum() < 0) {
                currentTotalBorrowed = currentTotalBorrowed.add(txn.abs());
                cust.setTotalBorrowedAmount(currentTotalBorrowed);
            }

            LocalDate today = LocalDate.now();
            if (cust.getDueDate() != null && cust.getDueDate().isBefore(today) && newDebt.signum() > 0) {
                cust.setStatus(Customer.BorrowStatus.OVERDUE);
            } else if (newDebt.signum() == 0) {
                cust.setStatus(Customer.BorrowStatus.PAID);
            } else {
                cust.setStatus(Customer.BorrowStatus.PARTIAL);
            }

            customerRepo.save(cust);
            h.setCumulativeAmount(newDebt);
            h = historyRepo.save(h);
            log.info("Transaction completed for custId={}, orgId={}", req.custId, organizationId);
            return toResponse(h);
        } catch (Exception ex) {
            log.error("Failed to add customer history for orgId={}, custId={}: {}", organizationId, req.custId, ex.getMessage(), ex);
            throw ex;
        }
    }

    @Override
    public Page<CustomerHistoryResponse> listByCustomer(String organizationId, String custId, Pageable pageable) {
        try {
            return historyRepo.findByOrganizationIdAndCustIdOrderByTransactionDateDesc(organizationId, custId, pageable)
                    .map(this::toResponse);
        } catch (Exception ex) {
            log.error("Error fetching history byCustomer orgId={} custId={}: {}", organizationId, custId, ex.getMessage(), ex);
            throw ex;
        }
    }

    @Override
    public Page<CustomerHistoryResponse> latestN(String organizationId, String custId, int n) {
        try {
            return listByCustomer(organizationId, custId, PageRequest.of(0, n));
        } catch (Exception ex) {
            log.error("Error fetching latestN orgId={} custId={} n={}: {}", organizationId, custId, n, ex.getMessage(), ex);
            throw ex;
        }
    }

    @Override
    public Page<CustomerHistoryResponse> listByOrg(String organizationId, Pageable pageable) {
        try {
            return historyRepo.findByOrganizationIdOrderByTransactionDateDesc(organizationId, pageable)
                    .map(this::toResponse);
        } catch (Exception ex) {
            log.error("Error fetching history listByOrg orgId={}: {}", organizationId, ex.getMessage(), ex);
            throw ex;
        }
    }

    @Override
    public Page<CustomerHistoryResponse> listByOrgToday(String organizationId, Pageable pageable) {
        try {
            LocalDate today = LocalDate.now(com.pulse.fineflux.utill.DateTimeUtil.IST);
            Instant start = today.atStartOfDay(com.pulse.fineflux.utill.DateTimeUtil.IST).toInstant();
            Instant end = today.plusDays(1).atStartOfDay(com.pulse.fineflux.utill.DateTimeUtil.IST).toInstant();
            return historyRepo.findByOrganizationIdAndTransactionDateBetweenOrderByTransactionDateDesc(
                    organizationId, start, end, pageable
            ).map(this::toResponse);
        } catch (Exception ex) {
            log.error("Error fetching history listByOrgToday orgId={}: {}", organizationId, ex.getMessage(), ex);
            throw ex;
        }
    }

    @Override
    public Page<CustomerHistoryResponse> listByOrgLastWeek(String organizationId, Pageable pageable) {
        try {
            LocalDate today = LocalDate.now(com.pulse.fineflux.utill.DateTimeUtil.IST);
            LocalDate weekAgo = today.minusDays(7);
            Instant start = weekAgo.atStartOfDay(com.pulse.fineflux.utill.DateTimeUtil.IST).toInstant();
            Instant end = today.plusDays(1).atStartOfDay(com.pulse.fineflux.utill.DateTimeUtil.IST).toInstant();
            return historyRepo.findByOrganizationIdAndTransactionDateBetweenOrderByTransactionDateDesc(
                    organizationId, start, end, pageable
            ).map(this::toResponse);
        } catch (Exception ex) {
            log.error("Error fetching history listByOrgLastWeek orgId={}: {}", organizationId, ex.getMessage(), ex);
            throw ex;
        }
    }

    @Override
    public Page<CustomerHistoryResponse> listByOrgMonth(String organizationId, Pageable pageable) {
        try {
            LocalDate today = LocalDate.now(com.pulse.fineflux.utill.DateTimeUtil.IST);
            LocalDate firstOfMonth = today.withDayOfMonth(1);
            Instant start = firstOfMonth.atStartOfDay(com.pulse.fineflux.utill.DateTimeUtil.IST).toInstant();
            Instant end = today.plusDays(1).atStartOfDay(com.pulse.fineflux.utill.DateTimeUtil.IST).toInstant();
            return historyRepo.findByOrganizationIdAndTransactionDateBetweenOrderByTransactionDateDesc(
                    organizationId, start, end, pageable
            ).map(this::toResponse);
        } catch (Exception ex) {
            log.error("Error fetching history listByOrgMonth orgId={}: {}", organizationId, ex.getMessage(), ex);
            throw ex;
        }
    }

    @Override
    public Page<CustomerHistoryResponse> listByOrgDateRange(String organizationId, Instant from, Instant to, Pageable pageable) {
        try {
            return historyRepo.findByOrganizationIdAndTransactionDateBetweenOrderByTransactionDateDesc(
                    organizationId, from, to, pageable
            ).map(this::toResponse);
        } catch (Exception ex) {
            log.error("Error fetching history listByOrgDateRange orgId={} from={} to={}: {}", organizationId, from, to, ex.getMessage(), ex);
            throw ex;
        }
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
