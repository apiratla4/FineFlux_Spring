// src/main/java/com/pulse/fineflux/service/CustomerServiceImpl.java
package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.CustomerCreateRequest;
import com.pulse.fineflux.domain.CustomerResponse;
import com.pulse.fineflux.domain.CustomerUpdateRequest;
import com.pulse.fineflux.entity.Customer;
import com.pulse.fineflux.repository.CustomerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repo;

    public CustomerServiceImpl(CustomerRepository repo) {
        this.repo = repo;
    }

    @Override
    public CustomerResponse create(String organizationId, CustomerCreateRequest req) {
        log.info("Creating customer orgId={} custId={} vehicle={}", organizationId, req.custId, req.customerVehicleNum);

        Customer.LifecycleStatus lifecycle = parseLifecycle(req.lifecycleStatus); // default ACTIVE [web:22]
        if (lifecycle == Customer.LifecycleStatus.INACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Customer is InActive; creation not allowed"); // 403 [web:14]
        }
        Customer c = new Customer();
        c.setOrganizationId(organizationId);
        c.setCustId(req.custId);
        c.setCustomerName(req.customerName);
        c.setCustomerVehicleNum(req.customerVehicleNum);
        c.setEmpId(req.empId);
        c.setAmountBorrowed(req.amountBorrowed);
        c.setTotalBorrowedAmount(req.amountBorrowed != null ? req.amountBorrowed : BigDecimal.ZERO);
        c.setBorrowDate(req.borrowDate);
        c.setDueDate(req.dueDate);
        c.setStatus(parseBorrowStatus(req.status));
        c.setLifecycleStatus(lifecycle);
        c.setPhoneNumber(req.phoneNumber);
        c.setEmail(req.email);
        c.setNotes(req.notes);
        c.setAddress(mapAddress(req.address));
        c = repo.save(c);
        log.info("Created customer id={} orgId={} custId={}", c.getId(), organizationId, c.getCustId());
        return toResponse(c);
    }

    @Override
    public long deleteAllForOrganization(String organizationId) {
        log.warn("Bulk delete customers for orgId={}", organizationId);
        long removed = repo.deleteByOrganizationId(organizationId);
        log.info("Bulk deleted {} customer(s) for orgId={}", removed, organizationId);
        return removed;
    }

    @Override
    public CustomerResponse get(String organizationId, String id) {
        log.debug("Fetching customer id={} orgId={}", id, organizationId);
        Customer c = repo.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        return toResponse(c);
    }

    @Override
    public Page<CustomerResponse> list(String organizationId, Pageable pageable) {
        log.debug("Listing customers orgId={} page={} size={}", organizationId, pageable.getPageNumber(), pageable.getPageSize());
        return repo.findAllByOrganizationId(organizationId, pageable).map(this::toResponse);
    }

    @Override
    public CustomerResponse update(String organizationId, String id, CustomerUpdateRequest req) {
        log.info("Updating customer id={} orgId={}", id, organizationId);
        Customer c = repo.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        if (req.organizationId != null && !organizationId.equals(req.organizationId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "organizationId change is not allowed");
        }

        // Compute target lifecycle after this update
        Customer.LifecycleStatus targetLifecycle = c.getLifecycleStatus() == null
                ? Customer.LifecycleStatus.ACTIVE
                : c.getLifecycleStatus();
        if (req.lifecycleStatus != null) {
            targetLifecycle = parseLifecycle(req.lifecycleStatus);
        }
        if (targetLifecycle == Customer.LifecycleStatus.INACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Customer is InActive; update not allowed"); // 403 [web:14]
        }

        if (req.custId != null) c.setCustId(req.custId);
        if (req.customerName != null) c.setCustomerName(req.customerName);
        if (req.customerVehicleNum != null) c.setCustomerVehicleNum(req.customerVehicleNum);
        if (req.empId != null) c.setEmpId(req.empId);
        if (req.amountBorrowed != null) c.setAmountBorrowed(req.amountBorrowed);
        if (req.totalBorrowedAmount != null) c.setTotalBorrowedAmount(req.totalBorrowedAmount);
        if (req.borrowDate != null) c.setBorrowDate(req.borrowDate);
        if (req.dueDate != null) c.setDueDate(req.dueDate);
        if (req.status != null) c.setStatus(parseBorrowStatus(req.status));
        if (req.phoneNumber != null) c.setPhoneNumber(req.phoneNumber);
        if (req.email != null) c.setEmail(req.email);
        if (req.notes != null) c.setNotes(req.notes);
        if (req.address != null) c.setAddress(mapAddress(req.address));
        // commit ACTIVE lifecycle
        c.setLifecycleStatus(targetLifecycle);

        c = repo.save(c);
        log.info("Updated customer id={} orgId={}", id, organizationId);
        return toResponse(c);
    }

    @Override
    public void delete(String organizationId, String id) {
        log.info("Deleting customer id={} orgId={}", id, organizationId);
        if (repo.findByIdAndOrganizationId(id, organizationId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
        }
        repo.deleteById(id);
        log.info("Deleted customer id={} orgId={}", id, organizationId);
    }

    @Override
    public void deleteByCustId(String organizationId, String custId) {
        log.info("Deleting customer by custId={} orgId={}", custId, organizationId);
        long removed = repo.deleteByCustIdAndOrganizationId(custId, organizationId);
        if (removed == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
        }
        log.info("Deleted customer custId={} orgId={}", custId, organizationId);
    }

    @Override
    public CustomerResponse updateTotalBorrowedAmount(String organizationId, String custId, BigDecimal totalBorrowedAmount) {
        Customer c = repo.findByCustIdAndOrganizationId(custId, organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        // optional: forbid if lifecycle is INACTIVE
        if (c.getLifecycleStatus() == Customer.LifecycleStatus.INACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Customer is InActive; update not allowed"); // 403 [web:14]
        }
        c.setTotalBorrowedAmount(totalBorrowedAmount);
        c = repo.save(c);
        return toResponse(c);
    }

    private Customer.BorrowStatus parseBorrowStatus(String s) {
        try {
            return Customer.BorrowStatus.valueOf(s.toUpperCase());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status: " + s);
        }
    }

    private Customer.LifecycleStatus parseLifecycle(String s) {
        if (s == null || s.isBlank()) return Customer.LifecycleStatus.ACTIVE; // default ACTIVE [web:22]
        String norm = s.trim().toUpperCase().replaceAll("[^A-Z]", ""); // normalize Active/InActive
        if ("ACTIVE".equals(norm)) return Customer.LifecycleStatus.ACTIVE;
        if ("INACTIVE".equals(norm)) return Customer.LifecycleStatus.INACTIVE;
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid lifecycleStatus: " + s);
    }

    @Override
    public List<CustomerResponse> getTodayCustomers(String organizationId) {
        LocalDateTime start = LocalDate.now(ZoneId.of("Asia/Kolkata")).atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        return repo.findAllByOrganizationIdAndBorrowDateBetween(organizationId, start, end)
                .stream().map(this::toResponse).toList();
    }

    @Override
    public List<CustomerResponse> getWeekCustomers(String organizationId) {
        LocalDateTime start = LocalDate.now(ZoneId.of("Asia/Kolkata"))
                .with(java.time.DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime end = start.plusDays(7);
        return repo.findAllByOrganizationIdAndBorrowDateBetween(organizationId, start, end)
                .stream().map(this::toResponse).toList();
    }

    @Override
    public List<CustomerResponse> getMonthCustomers(String organizationId) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        LocalDateTime start = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = start.plusMonths(1);
        return repo.findAllByOrganizationIdAndBorrowDateBetween(organizationId, start, end)
                .stream().map(this::toResponse).toList();
    }

    @Override
    public List<CustomerResponse> getCustomersByDateRange(String organizationId, LocalDateTime from, LocalDateTime to) {
        return repo.findAllByOrganizationIdAndBorrowDateBetween(organizationId, from, to)
                .stream().map(this::toResponse).toList();
    }


    // Update ONLY the lifecycleStatus of a customer
    @Override
    public CustomerResponse updateLifecycleStatus(String organizationId, String id, String lifecycleStatus) {
        Customer c = repo.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        Customer.LifecycleStatus status = parseLifecycle(lifecycleStatus);
        c.setLifecycleStatus(status);

        c = repo.save(c);
        log.info("Updated lifecycleStatus for customer id={} orgId={} to {}", id, organizationId, status);
        return toResponse(c);
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
        r.lifecycleStatus = c.getLifecycleStatus() != null ? c.getLifecycleStatus().name() : null;
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
