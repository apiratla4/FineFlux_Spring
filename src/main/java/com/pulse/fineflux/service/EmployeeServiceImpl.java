// src/main/java/com/pulse/fineflux/service/impl/EmployeeServiceImpl.java
package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.EmployeeCreateRequest;
import com.pulse.fineflux.domain.EmployeeResponse;
import com.pulse.fineflux.domain.EmployeeUpdateRequest;
import com.pulse.fineflux.entity.Employee;
import com.pulse.fineflux.repository.EmployeeRepository;
import com.pulse.fineflux.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
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
    public EmployeeResponse create(String organizationId, EmployeeCreateRequest req) {
        log.info("Creating employee orgId={} empId={} username={}", organizationId, req.empId, req.username);

        if (repo.existsByEmpId(req.empId)) {
            log.warn("empId already exists empId={} orgId={}", req.empId, organizationId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "empId already exists");
        }
        if (repo.existsByEmailId(req.emailId)) {
            log.warn("email already exists emailId={} orgId={}", req.emailId, organizationId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }
        if (repo.existsByOrganizationIdAndUsername(organizationId, req.username)) {
            log.warn("username already exists per org username={} orgId={}", req.username, organizationId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists in this organization");
        }

        Employee e = new Employee();
        e.setOrganizationId(organizationId);
        e.setEmpId(req.empId);

        // Normalize status to uppercase; default to ACTIVE if missing
        e.setStatus(normalizeStatusOrDefault(req.status));

        e.setRole(req.role);
        e.setDepartment(req.department);
        e.setFirstName(req.firstName);
        e.setLastName(req.lastName);
        e.setPhoneNumber(req.phoneNumber);
        e.setEmailId(req.emailId);
        e.setUsername(req.username);
        e.setPasswordHash(passwordEncoder.encode(req.password));

        // NEW FIELDS
        e.setGender(req.gender);
        e.setSalary(req.salary);

        e.setShiftTiming(mapShift(req.shiftTiming));
        e.setAddress(mapAddress(req.address));
        e.setEmergencyContact(mapEC(req.emergencyContact));

        e = repo.save(e);
        log.info("Created employee id={} orgId={} empId={}", e.getId(), organizationId, e.getEmpId());
        return toResponse(e);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse get(String organizationId, String id) {
        log.debug("Fetching employee id={} orgId={}", id, organizationId);
        Employee e = repo.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> {
                    log.warn("Employee not found id={} orgId={}", id, organizationId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found");
                });
        return toResponse(e);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> list(String organizationId, Pageable pageable) {
        log.debug("Listing employees orgId={} page={} size={}", organizationId, pageable.getPageNumber(), pageable.getPageSize());
        return repo.findAllByOrganizationId(organizationId, pageable).map(this::toResponse);
    }

    @Override
    public EmployeeResponse update(String organizationId, String id, EmployeeUpdateRequest req) {
        log.info("Updating employee id={} orgId={}", id, organizationId);
        Employee e = repo.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> {
                    log.warn("Employee not found id={} orgId={}", id, organizationId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found");
                });

        if (req.organizationId != null && !organizationId.equals(req.organizationId)) {
            log.warn("Attempted organizationId change for employee id={} from={} to={}", id, organizationId, req.organizationId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "organizationId change is not allowed");
        }

        if (req.empId != null && !req.empId.equals(e.getEmpId())) {
            if (repo.existsByEmpId(req.empId)) {
                log.warn("empId already exists empId={} orgId={}", req.empId, organizationId);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "empId already exists");
            }
            e.setEmpId(req.empId);
        }

        // Update status when provided
        if (req.status != null && !req.status.isBlank()) {
            e.setStatus(normalizeStatus(req.status));
        }

        if (req.role != null) e.setRole(req.role);
        if (req.department != null) e.setDepartment(req.department);
        if (req.firstName != null) e.setFirstName(req.firstName);
        if (req.lastName != null) e.setLastName(req.lastName);
        if (req.phoneNumber != null) e.setPhoneNumber(req.phoneNumber);

        if (req.emailId != null && !req.emailId.equals(e.getEmailId())) {
            if (repo.existsByEmailId(req.emailId)) {
                log.warn("email already exists emailId={} orgId={}", req.emailId, organizationId);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
            }
            e.setEmailId(req.emailId);
        }
        if (req.username != null && !req.username.equals(e.getUsername())) {
            if (repo.existsByOrganizationIdAndUsername(organizationId, req.username)) {
                log.warn("username already exists per org username={} orgId={}", req.username, organizationId);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists in this organization");
            }
            e.setUsername(req.username);
        }
        if (req.newPassword != null && !req.newPassword.isBlank()) {
            e.setPasswordHash(passwordEncoder.encode(req.newPassword));
        }

        // NEW FIELDS UPDATE
        if (req.gender != null) e.setGender(req.gender);
        if (req.salary != null) e.setSalary(req.salary);

        if (req.shiftTiming != null) e.setShiftTiming(mapShift(req.shiftTiming));
        if (req.address != null) e.setAddress(mapAddress(req.address));
        if (req.emergencyContact != null) e.setEmergencyContact(mapEC(req.emergencyContact));

        e = repo.save(e);
        log.info("Updated employee id={} orgId={} empId={}", id, organizationId, e.getEmpId());
        return toResponse(e);
    }

    @Override
    public void delete(String organizationId, String id) {
        log.info("Deleting employee id={} orgId={}", id, organizationId);
        if (!repo.existsByIdAndOrganizationId(id, organizationId)) {
            log.warn("Delete failed, employee not found id={} orgId={}", id, organizationId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found");
        }
        repo.deleteById(id);
        log.info("Deleted employee id={} orgId={}", id, organizationId);
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
        r.empId = e.getEmpId();
        r.organizationId = e.getOrganizationId();
        r.status = e.getStatus();
        r.role = e.getRole();
        r.department = e.getDepartment();
        r.firstName = e.getFirstName();
        r.lastName = e.getLastName();
        r.phoneNumber = e.getPhoneNumber();
        r.emailId = e.getEmailId();
        r.username = e.getUsername();

        // NEW FIELDS
        r.gender = e.getGender();
        r.salary = e.getSalary();

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

    private String normalizeStatusOrDefault(String s) {
        return (s == null || s.isBlank()) ? "ACTIVE" : normalizeStatus(s);
    }

    private String normalizeStatus(String s) {
        String u = s.toUpperCase(java.util.Locale.ROOT);
        if (!u.equals("ACTIVE") && !u.equals("INACTIVE")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status must be ACTIVE or INACTIVE");
        }
        return u;
    }
}
