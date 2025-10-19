// controller/EmployeeDutyController.java
package com.pulse.fineflux.controller;

import com.pulse.fineflux.domain.EmployeeDutyCreateDTO;
import com.pulse.fineflux.domain.EmployeeDutyResponseDTO;
import com.pulse.fineflux.domain.EmployeeDutyUpdateDTO;
import com.pulse.fineflux.service.EmployeeDutyService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/organizations/{orgId}/employee-duties")
@CrossOrigin(origins = "*")
public class EmployeeDutyController {

    @Autowired
    private EmployeeDutyService dutyService;

    // ==================== CRUD OPERATIONS ====================

    @PostMapping
    public ResponseEntity<EmployeeDutyResponseDTO> createDuty(
            @PathVariable String orgId,
            @Valid @RequestBody EmployeeDutyCreateDTO createDTO) {
        createDTO.setOrganizationId(orgId);
        EmployeeDutyResponseDTO response = dutyService.createDuty(createDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<EmployeeDutyResponseDTO>> getAllDutiesByOrganization(
            @PathVariable String orgId) {
        List<EmployeeDutyResponseDTO> duties = dutyService.getDutiesByOrganizationId(orgId);
        return ResponseEntity.ok(duties);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDutyResponseDTO> getDutyById(@PathVariable String id) {
        EmployeeDutyResponseDTO duty = dutyService.getDutyById(id);
        return ResponseEntity.ok(duty);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDutyResponseDTO> updateDuty(
            @PathVariable String id,
            @Valid @RequestBody EmployeeDutyUpdateDTO updateDTO) {
        EmployeeDutyResponseDTO updated = dutyService.updateDuty(id, updateDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDuty(@PathVariable String id) {
        dutyService.deleteDuty(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== QUERY BY EMPLOYEE ====================

    @GetMapping("/employee/{empId}")
    public ResponseEntity<List<EmployeeDutyResponseDTO>> getDutiesByEmployee(
            @PathVariable String orgId,
            @PathVariable String empId) {
        List<EmployeeDutyResponseDTO> duties = dutyService.getDutiesByOrganizationAndEmployee(orgId, empId);
        return ResponseEntity.ok(duties);
    }

    @GetMapping("/employee/{empId}/date-range")
    public ResponseEntity<List<EmployeeDutyResponseDTO>> getDutiesByEmployeeAndDateRange(
            @PathVariable String empId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<EmployeeDutyResponseDTO> duties = dutyService.getDutiesByEmployeeAndDateRange(empId, startDate, endDate);
        return ResponseEntity.ok(duties);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<EmployeeDutyResponseDTO>> getDutiesByStatus(
            @PathVariable String orgId,
            @PathVariable String status) {
        List<EmployeeDutyResponseDTO> duties = dutyService.getDutiesByStatus(orgId, status);
        return ResponseEntity.ok(duties);
    }

    // ==================== DATE-BASED QUERIES FOR ORGANIZATION ====================

    @GetMapping("/today")
    public ResponseEntity<List<EmployeeDutyResponseDTO>> getDutiesForToday(
            @PathVariable String orgId) {
        List<EmployeeDutyResponseDTO> duties = dutyService.getDutiesForToday(orgId);
        return ResponseEntity.ok(duties);
    }

    @GetMapping("/this-week")
    public ResponseEntity<List<EmployeeDutyResponseDTO>> getDutiesForThisWeek(
            @PathVariable String orgId) {
        List<EmployeeDutyResponseDTO> duties = dutyService.getDutiesForThisWeek(orgId);
        return ResponseEntity.ok(duties);
    }

    @GetMapping("/this-month")
    public ResponseEntity<List<EmployeeDutyResponseDTO>> getDutiesForThisMonth(
            @PathVariable String orgId) {
        List<EmployeeDutyResponseDTO> duties = dutyService.getDutiesForThisMonth(orgId);
        return ResponseEntity.ok(duties);
    }

    @GetMapping("/custom-range")
    public ResponseEntity<List<EmployeeDutyResponseDTO>> getDutiesByCustomDateRange(
            @PathVariable String orgId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<EmployeeDutyResponseDTO> duties = dutyService.getDutiesByCustomDateRange(orgId, startDate, endDate);
        return ResponseEntity.ok(duties);
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<EmployeeDutyResponseDTO>> getDutiesBySpecificDate(
            @PathVariable String orgId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<EmployeeDutyResponseDTO> duties = dutyService.getDutiesByDate(orgId, date);
        return ResponseEntity.ok(duties);
    }

    // ==================== DATE-BASED QUERIES FOR SPECIFIC EMPLOYEE ====================

    @GetMapping("/employee/{empId}/today")
    public ResponseEntity<List<EmployeeDutyResponseDTO>> getEmployeeDutiesForToday(
            @PathVariable String orgId,
            @PathVariable String empId) {
        List<EmployeeDutyResponseDTO> duties = dutyService.getEmployeeDutiesForToday(orgId, empId);
        return ResponseEntity.ok(duties);
    }

    @GetMapping("/employee/{empId}/this-week")
    public ResponseEntity<List<EmployeeDutyResponseDTO>> getEmployeeDutiesForThisWeek(
            @PathVariable String orgId,
            @PathVariable String empId) {
        List<EmployeeDutyResponseDTO> duties = dutyService.getEmployeeDutiesForThisWeek(orgId, empId);
        return ResponseEntity.ok(duties);
    }

    @GetMapping("/employee/{empId}/this-month")
    public ResponseEntity<List<EmployeeDutyResponseDTO>> getEmployeeDutiesForThisMonth(
            @PathVariable String orgId,
            @PathVariable String empId) {
        List<EmployeeDutyResponseDTO> duties = dutyService.getEmployeeDutiesForThisMonth(orgId, empId);
        return ResponseEntity.ok(duties);
    }

    @GetMapping("/employee/{empId}/custom-range")
    public ResponseEntity<List<EmployeeDutyResponseDTO>> getEmployeeDutiesByCustomDateRange(
            @PathVariable String orgId,
            @PathVariable String empId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<EmployeeDutyResponseDTO> duties = dutyService.getEmployeeDutiesByCustomDateRange(
                orgId, empId, startDate, endDate);
        return ResponseEntity.ok(duties);
    }

    // ==================== ADVANCED QUERIES ====================

    @GetMapping("/filter")
    public ResponseEntity<List<EmployeeDutyResponseDTO>> getDutiesByDateRangeAndStatus(
            @PathVariable String orgId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam String status) {
        List<EmployeeDutyResponseDTO> duties = dutyService.getDutiesByDateRangeAndStatus(
                orgId, startDate, endDate, status);
        return ResponseEntity.ok(duties);
    }
}
