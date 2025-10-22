package com.pulse.fineflux.service.impl;

import com.pulse.fineflux.domain.CustomerCreateRequest;
import com.pulse.fineflux.domain.CustomerResponse;
import com.pulse.fineflux.domain.CustomerUpdateRequest;
import com.pulse.fineflux.entity.Customer;
import com.pulse.fineflux.entity.CustomerHistory;
import com.pulse.fineflux.repository.CustomerRepository;
import com.pulse.fineflux.repository.CustomerHistoryRepository;
import com.pulse.fineflux.service.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;

@Slf4j
@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repo;
    private final CustomerHistoryRepository historyRepo;

    public CustomerServiceImpl(CustomerRepository repo, CustomerHistoryRepository historyRepo) {
        this.repo = repo;
        this.historyRepo = historyRepo;
    }

    @Override
    @Transactional
    public CustomerResponse create(String organizationId, CustomerCreateRequest req) {
        log.info("Creating customer orgId={} custId={} vehicle={}", organizationId, req.custId, req.customerVehicleNum);

        // Validate unique custId
        if (repo.findByCustIdAndOrganizationId(req.custId, organizationId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Customer ID already exists: " + req.custId);
        }

        Customer c = new Customer();
        c.setOrganizationId(organizationId);
        c.setCustId(req.custId);
        c.setCustomerName(req.customerName);
        c.setCustomerVehicleNum(req.customerVehicleNum);
        c.setEmpId(req.empId);
        c.setAmountBorrowed(req.amountBorrowed != null ? req.amountBorrowed : BigDecimal.ZERO);
        c.setTotalBorrowedAmount(req.amountBorrowed != null ? req.amountBorrowed : BigDecimal.ZERO);
        c.setBorrowDate(req.borrowDate);
        c.setDueDate(req.dueDate);
        c.setStatus(parseStatus(req.status));
        c.setPhoneNumber(req.phoneNumber);
        c.setEmail(req.email);
        c.setNotes(req.notes);
        c.setAddress(mapAddress(req.address));

        c = repo.save(c);
        log.info("Created customer id={} orgId={} custId={}", c.getId(), organizationId, c.getCustId());

        // ✅ AUTO-CREATE OPENING BALANCE HISTORY (Only once, only if amount > 0)
        if (c.getAmountBorrowed() != null && c.getAmountBorrowed().compareTo(BigDecimal.ZERO) > 0) {
            CustomerHistory history = new CustomerHistory();
            history.setOrganizationId(organizationId);
            history.setCustomerId(c.getId());
            history.setCustId(c.getCustId());
            history.setTransactionAmount(c.getAmountBorrowed().negate()); // Negative = borrowed
            history.setTransactionDate(Instant.now());
            history.setCumulativeAmount(c.getAmountBorrowed());
            history.setNotes("Opening balance on customer creation");
            historyRepo.save(history);
            log.info("Created opening balance history for custId={} amount={}", c.getCustId(), c.getAmountBorrowed());
        }

        return toResponse(c);
    }

    @Override
    public Page<CustomerResponse> list(String organizationId, Pageable pageable) {
        log.debug("Listing customers orgId={}", organizationId);
        return repo.findAllByOrganizationId(organizationId, pageable).map(this::toResponse);
    }

    @Override
    public CustomerResponse get(String organizationId, String id) {
        log.debug("Fetching customer id={} orgId={}", id, organizationId);
        Customer c = repo.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        return toResponse(c);
    }

    // ✅ NEW: Date-based filters
    @Override
    public Page<CustomerResponse> listByDate(String organizationId, LocalDate date, Pageable pageable) {
        log.debug("Listing customers by date orgId={} date={}", organizationId, date);
        return repo.findByOrganizationIdAndBorrowDate(organizationId, date, pageable).map(this::toResponse);
    }

    @Override
    public Page<CustomerResponse> listByDateRange(String organizationId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        log.debug("Listing customers by date range orgId={} from={} to={}", organizationId, startDate, endDate);
        return repo.findByOrganizationIdAndBorrowDateBetween(organizationId, startDate, endDate, pageable).map(this::toResponse);
    }

    @Override
    public Page<CustomerResponse> listToday(String organizationId, Pageable pageable) {
        LocalDate today = LocalDate.now();
        log.debug("Listing today's customers orgId={} date={}", organizationId, today);
        return listByDate(organizationId, today, pageable);
    }

    @Override
    public Page<CustomerResponse> listThisWeek(String organizationId, Pageable pageable) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);
        log.debug("Listing this week's customers orgId={} from={} to={}", organizationId, startOfWeek, endOfWeek);
        return listByDateRange(organizationId, startOfWeek, endOfWeek, pageable);
    }

    @Override
    public Page<CustomerResponse> listThisMonth(String organizationId, Pageable pageable) {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());
        log.debug("Listing this month's customers orgId={} from={} to={}", organizationId, startOfMonth, endOfMonth);
        return listByDateRange(organizationId, startOfMonth, endOfMonth, pageable);
    }

    @Override
    @Transactional
    public CustomerResponse update(String organizationId, String id, CustomerUpdateRequest req) {
        log.info("Updating customer id={} orgId={}", id, organizationId);
        Customer c = repo.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        if (req.organizationId != null && !organizationId.equals(req.organizationId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "organizationId change is not allowed");
        }

        if (req.custId != null) c.setCustId(req.custId);
        if (req.customerName != null) c.setCustomerName(req.customerName);
        if (req.customerVehicleNum != null) c.setCustomerVehicleNum(req.customerVehicleNum);
        if (req.empId != null) c.setEmpId(req.empId);
        if (req.amountBorrowed != null) c.setAmountBorrowed(req.amountBorrowed);
        if (req.totalBorrowedAmount != null) c.setTotalBorrowedAmount(req.totalBorrowedAmount);
        if (req.borrowDate != null) c.setBorrowDate(req.borrowDate);
        if (req.dueDate != null) c.setDueDate(req.dueDate);
        if (req.status != null) c.setStatus(parseStatus(req.status));
        if (req.phoneNumber != null) c.setPhoneNumber(req.phoneNumber);
        if (req.email != null) c.setEmail(req.email);
        if (req.notes != null) c.setNotes(req.notes);
        if (req.address != null) c.setAddress(mapAddress(req.address));

        c = repo.save(c);
        log.info("Updated customer id={} orgId={}", id, organizationId);
        return toResponse(c);
    }

    @Override
    @Transactional
    public void delete(String organizationId, String id) {
        log.info("Deleting customer id={} orgId={}", id, organizationId);
        if (repo.findByIdAndOrganizationId(id, organizationId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
        }
        repo.deleteById(id);
        log.info("Deleted customer id={} orgId={}", id, organizationId);
    }

    @Override
    @Transactional
    public void deleteByCustId(String organizationId, String custId) {
        log.info("Deleting customer by custId={} orgId={}", custId, organizationId);
        long removed = repo.deleteByCustIdAndOrganizationId(custId, organizationId);
        if (removed == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
        }
        log.info("Deleted customer custId={} orgId={}", custId, organizationId);
    }

    @Override
    public long deleteAllForOrganization(String organizationId) {
        log.warn("Bulk delete customers for orgId={}", organizationId);
        long removed = repo.deleteByOrganizationId(organizationId);
        log.info("Bulk deleted {} customer(s) for orgId={}", removed, organizationId);
        return removed;
    }

    @Override
    @Transactional
    public CustomerResponse updateTotalBorrowedAmount(String organizationId, String custId, BigDecimal totalBorrowedAmount) {
        Customer c = repo.findByCustIdAndOrganizationId(custId, organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        c.setTotalBorrowedAmount(totalBorrowedAmount);
        c = repo.save(c);
        return toResponse(c);
    }

    private Customer.BorrowStatus parseStatus(String s) {
        try {
            return Customer.BorrowStatus.valueOf(s.toUpperCase());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status: " + s);
        }
    }

    private Customer.Address mapAddress(CustomerCreateRequest.AddressDTO dto) {
        if (dto == null) return null;
        return Customer.Address.builder()
                .line1(dto.line1)
                .line2(dto.line2)
                .city(dto.city)
                .state(dto.state)
                .postalCode(dto.postalCode)
                .country(dto.country)
                .build();
    }

    private CustomerResponse toResponse(Customer c) {
        CustomerResponse r = new CustomerResponse();
        r.id = c.getId();
        r.organizationId = c.getOrganizationId();
        r.custId = c.getCustId();
        r.customerName = c.getCustomerName();
        r.customerVehicleNum = c.getCustomerVehicleNum();
        r.empId = c.getEmpId();
        r.amountBorrowed = c.getAmountBorrowed();
        r.totalBorrowedAmount = c.getTotalBorrowedAmount();
        r.borrowDate = c.getBorrowDate();
        r.dueDate = c.getDueDate();
        r.status = c.getStatus() != null ? c.getStatus().name() : null;
        r.phoneNumber = c.getPhoneNumber();
        r.email = c.getEmail();
        r.notes = c.getNotes();
        if (c.getAddress() != null) {
            CustomerCreateRequest.AddressDTO a = new CustomerCreateRequest.AddressDTO();
            a.line1 = c.getAddress().getLine1();
            a.line2 = c.getAddress().getLine2();
            a.city = c.getAddress().getCity();
            a.state = c.getAddress().getState();
            a.postalCode = c.getAddress().getPostalCode();
            a.country = c.getAddress().getCountry();
            r.address = a;
        }
        return r;
    }
}
