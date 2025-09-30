// src/main/java/com/pulse/fineflux/service/impl/EmployeeServiceImpl.java
package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.*;
import com.pulse.fineflux.entity.Employee;
import com.pulse.fineflux.repository.EmployeeRepository;
import com.pulse.fineflux.service.EmployeeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository repo;
    private final PasswordEncoder passwordEncoder;

    public EmployeeServiceImpl(EmployeeRepository repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public EmployeeResponse create(EmployeeCreateRequest req) {
        if (repo.existsByUsername(req.username)) throw new IllegalArgumentException("Username already exists");
        if (repo.existsByEmailId(req.emailId)) throw new IllegalArgumentException("Email already exists");

        Employee e = new Employee();
        e.setRole(req.role);
        e.setDepartment(req.department);
        e.setFirstName(req.firstName);
        e.setLastName(req.lastName);
        e.setPhoneNumber(req.phoneNumber);
        e.setEmailId(req.emailId);
        e.setUsername(req.username);
        e.setPasswordHash(passwordEncoder.encode(req.password));
        e.setShiftTiming(mapShift(req.shiftTiming));
        e.setAddress(mapAddress(req.address));
        e.setEmergencyContact(mapEC(req.emergencyContact));
        e = repo.save(e);

        return toResponse(e);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse get(String id) {
        Employee e = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Not found"));
        return toResponse(e);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> list(Pageable pageable) {
        return repo.findAll(pageable).map(this::toResponse);
    }

    @Override
    public EmployeeResponse update(String id, EmployeeUpdateRequest req) {
        Employee e = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Not found"));

        if (req.role != null) e.setRole(req.role);
        if (req.department != null) e.setDepartment(req.department);
        if (req.firstName != null) e.setFirstName(req.firstName);
        if (req.lastName != null) e.setLastName(req.lastName);
        if (req.phoneNumber != null) e.setPhoneNumber(req.phoneNumber);

        if (req.emailId != null && !req.emailId.equals(e.getEmailId())) {
            if (repo.existsByEmailId(req.emailId)) throw new IllegalArgumentException("Email already exists");
            e.setEmailId(req.emailId);
        }
        if (req.username != null && !req.username.equals(e.getUsername())) {
            if (repo.existsByUsername(req.username)) throw new IllegalArgumentException("Username already exists");
            e.setUsername(req.username);
        }
        if (req.newPassword != null && !req.newPassword.isBlank()) {
            e.setPasswordHash(passwordEncoder.encode(req.newPassword));
        }
        if (req.shiftTiming != null) e.setShiftTiming(mapShift(req.shiftTiming));
        if (req.address != null) e.setAddress(mapAddress(req.address));
        if (req.emergencyContact != null) e.setEmergencyContact(mapEC(req.emergencyContact));

        e = repo.save(e);
        return toResponse(e);
    }

    @Override
    public void delete(String id) {
        if (!repo.existsById(id)) throw new IllegalArgumentException("Not found");
        repo.deleteById(id);
    }

    private Employee.ShiftTiming mapShift(EmployeeCreateRequest.ShiftTimingDTO dto) {
        if (dto == null) return null;
        Employee.ShiftTiming s = new Employee.ShiftTiming();
        s.setStart(dto.start);
        s.setEnd(dto.end);
        return s;
    }

    private Employee.Address mapAddress(EmployeeCreateRequest.AddressDTO dto) {
        if (dto == null) return null;
        Employee.Address a = new Employee.Address();
        a.setLine1(dto.line1);
        a.setLine2(dto.line2);
        a.setCity(dto.city);
        a.setState(dto.state);
        a.setPostalCode(dto.postalCode);
        a.setCountry(dto.country);
        return a;
    }

    private Employee.EmergencyContact mapEC(EmployeeCreateRequest.EmergencyContactDTO dto) {
        if (dto == null) return null;
        Employee.EmergencyContact ec = new Employee.EmergencyContact();
        ec.setName(dto.name);
        ec.setPhone(dto.phone);
        ec.setRelationship(dto.relationship);
        return ec;
    }

    private EmployeeResponse toResponse(Employee e) {
        EmployeeResponse r = new EmployeeResponse();
        r.id = e.getId();
        r.role = e.getRole();
        r.department = e.getDepartment();
        r.firstName = e.getFirstName();
        r.lastName = e.getLastName();
        r.phoneNumber = e.getPhoneNumber();
        r.emailId = e.getEmailId();
        r.username = e.getUsername();
        r.joinedDate = e.getJoinedDate();
        if (e.getShiftTiming() != null) {
            EmployeeCreateRequest.ShiftTimingDTO s = new EmployeeCreateRequest.ShiftTimingDTO();
            s.start = e.getShiftTiming().getStart();
            s.end = e.getShiftTiming().getEnd();
            r.shiftTiming = s;
        }
        if (e.getAddress() != null) {
            EmployeeCreateRequest.AddressDTO a = new EmployeeCreateRequest.AddressDTO();
            a.line1 = e.getAddress().getLine1();
            a.line2 = e.getAddress().getLine2();
            a.city = e.getAddress().getCity();
            a.state = e.getAddress().getState();
            a.postalCode = e.getAddress().getPostalCode();
            a.country = e.getAddress().getCountry();
            r.address = a;
        }
        if (e.getEmergencyContact() != null) {
            EmployeeCreateRequest.EmergencyContactDTO ec = new EmployeeCreateRequest.EmergencyContactDTO();
            ec.name = e.getEmergencyContact().getName();
            ec.phone = e.getEmergencyContact().getPhone();
            ec.relationship = e.getEmergencyContact().getRelationship();
            r.emergencyContact = ec;
        }
        return r;
    }
}
