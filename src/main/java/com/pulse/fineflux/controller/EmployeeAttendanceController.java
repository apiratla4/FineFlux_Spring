package com.pulse.fineflux.controller;

import com.pulse.fineflux.domain.EmployeeAttendanceCreateDTO;
import com.pulse.fineflux.domain.EmployeeAttendanceUpdateDTO;
import com.pulse.fineflux.domain.EmployeeAttendanceResponseDTO;
import com.pulse.fineflux.service.EmployeeAttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/organizations/{organizationId}/attendance")
public class EmployeeAttendanceController {

    @Autowired
    private EmployeeAttendanceService service;

    // Create attendance
    @PostMapping
    public EmployeeAttendanceResponseDTO create(
            @PathVariable String organizationId,
            @RequestBody EmployeeAttendanceCreateDTO dto) {
        dto.setOrganizationId(organizationId);
        return service.create(dto);
    }

    // Update attendance
    @PutMapping("/{id}")
    public EmployeeAttendanceResponseDTO update(
            @PathVariable String organizationId,
            @PathVariable String id,
            @RequestBody EmployeeAttendanceUpdateDTO dto) {
        // (Validation of orgId can be added here if needed)
        return service.update(id, dto);
    }

    // Delete attendance
    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable String organizationId,
            @PathVariable String id) {
        service.delete(id);
    }

    // Get by id
    @GetMapping("/{id}")
    public EmployeeAttendanceResponseDTO getById(
            @PathVariable String organizationId,
            @PathVariable String id) {
        return service.getById(id);
    }

    // Get all for org
    @GetMapping
    public List<EmployeeAttendanceResponseDTO> getAllByOrganizationId(
            @PathVariable String organizationId) {
        return service.getAll().stream()
                .filter(a -> organizationId.equals(a.getOrganizationId()))
                .toList();
    }

    // Get all for employee
    @GetMapping("/employee/{empId}")
    public List<EmployeeAttendanceResponseDTO> getByEmpId(
            @PathVariable String organizationId,
            @PathVariable String empId) {
        return service.getByEmpId(empId).stream()
                .filter(a -> organizationId.equals(a.getOrganizationId()))
                .toList();
    }

    private static final DateTimeFormatter ISO_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    // Get all attendances in a date range (example for report usages)
    @GetMapping("/daterange")
    public List<EmployeeAttendanceResponseDTO> getByDateRange(
            @PathVariable String organizationId,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        return service.getAll().stream()
                .filter(a -> organizationId.equals(a.getOrganizationId()))
                .filter(a -> {
                    String checkInStr = a.getCheckIn();
                    if (checkInStr == null) return false;
                    try {
                        LocalDateTime checkIn = LocalDateTime.parse(checkInStr, ISO_FORMAT);
                        return (checkIn.equals(start) || checkIn.isAfter(start)) && (checkIn.equals(end) || checkIn.isBefore(end));
                    } catch (Exception e) {
                        // If parsing fails, skip this record
                        return false;
                    }
                })
                .toList();
    }
}
