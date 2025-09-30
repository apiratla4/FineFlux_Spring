// src/main/java/com/pulse/fineflux/service/impl/CustomerServiceImpl.java
package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.CustomerCreateRequest;
import com.pulse.fineflux.domain.CustomerResponse;
import com.pulse.fineflux.domain.CustomerUpdateRequest;
import com.pulse.fineflux.entity.Customer;
import com.pulse.fineflux.repository.CustomerRepository;
import com.pulse.fineflux.service.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repo;

    public CustomerServiceImpl(CustomerRepository repo) {
        this.repo = repo;
    }

    @Override
    public CustomerResponse create(String organizationId, CustomerCreateRequest req) {
        log.info("Creating customer orgId={} vehicle={}", organizationId, req.customerVehicleNum);
        Customer c = new Customer();
        c.setOrganizationId(organizationId);
        c.setCustomerName(req.customerName);
        c.setCustomerVehicleNum(req.customerVehicleNum);
        c.setEmpId(req.empId);
        c.setAmountBorrowed(req.amountBorrowed);
        c.setBorrowDate(req.borrowDate);
        c.setDueDate(req.dueDate);
        c.setStatus(parseStatus(req.status));
        c.setPhoneNumber(req.phoneNumber);
        c.setEmail(req.email);
        c.setNotes(req.notes);
        c.setAddress(mapAddress(req.address));
        c = repo.save(c);
        log.info("Created customer id={} orgId={}", c.getId(), organizationId);
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
                .orElseThrow(() -> {
                    log.warn("Customer not found id={} orgId={}", id, organizationId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
                });
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
                .orElseThrow(() -> {
                    log.warn("Customer not found id={} orgId={}", id, organizationId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
                });

        if (req.organizationId != null && !organizationId.equals(req.organizationId)) {
            log.warn("Attempted organizationId change for customer id={} from={} to={}", id, organizationId, req.organizationId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "organizationId change is not allowed");
        }

        if (req.customerName != null) c.setCustomerName(req.customerName);
        if (req.customerVehicleNum != null) c.setCustomerVehicleNum(req.customerVehicleNum);
        if (req.empId != null) c.setEmpId(req.empId);
        if (req.amountBorrowed != null) c.setAmountBorrowed(req.amountBorrowed);
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
    public void delete(String organizationId, String id) {
        log.info("Deleting customer id={} orgId={}", id, organizationId);
        if (!repo.existsByIdAndOrganizationId(id, organizationId)) {
            log.warn("Delete failed, customer not found id={} orgId={}", id, organizationId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
        }
        repo.deleteById(id);
        log.info("Deleted customer id={} orgId={}", id, organizationId);
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
        r.customerName = c.getCustomerName();
        r.customerVehicleNum = c.getCustomerVehicleNum();
        r.empId = c.getEmpId();
        r.amountBorrowed = c.getAmountBorrowed();
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
