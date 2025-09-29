// src/main/java/com/pulse/fineflux/service/impl/CustomerServiceImpl.java
package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.*;
import com.pulse.fineflux.entity.Customer;
import com.pulse.fineflux.repository.CustomerRepository;
import com.pulse.fineflux.service.CustomerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repo;

    public CustomerServiceImpl(CustomerRepository repo) {
        this.repo = repo;
    }

    @Override
    public CustomerResponse create(CustomerCreateRequest req) {
        Customer c = new Customer();
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
        return toResponse(c);
    }

    @Override
    public CustomerResponse get(String id) {
        Customer c = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        return toResponse(c);
    }

    @Override
    public Page<CustomerResponse> list(Pageable pageable) {
        return repo.findAll(pageable).map(this::toResponse);
    }

    @Override
    public CustomerResponse update(String id, CustomerUpdateRequest req) {
        Customer c = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

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
        return toResponse(c);
    }

    @Override
    public void delete(String id) {
        if (!repo.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
        repo.deleteById(id);
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
