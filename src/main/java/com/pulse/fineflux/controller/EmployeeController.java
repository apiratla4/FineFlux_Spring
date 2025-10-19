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

    @PostMapping
    public ResponseEntity<EmployeeDutyResponseDTO> createDuty(
            @PathVariable String orgId,
            @Valid @RequestBody EmployeeDutyCreateDTO createDTO) {
        createDTO.setOrgId(orgId);
        EmployeeDutyResponseDTO response = dutyService.createDuty(createDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<EmployeeDutyResponseDTO>> getAllDutiesByOrganization(
            @PathVariable String orgId) {
        List<EmployeeDutyResponseDTO> duties = dutyService.getDutiesByOrgId(orgId);
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

    @GetMapping("/employee/{empId}")
    public ResponseEntity<List<EmployeeDutyResponseDTO>> getDutiesByEmployee(
            @PathVariable String orgId,
            @PathVariable String empId) {
        List<EmployeeDutyResponseDTO> duties = dutyService.getDutiesByOrgAndEmployee(orgId, empId);
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

    // New endpoints for time-based filtering
    @GetMapping("/today")
    public ResponseEntity<List<EmployeeDutyResponseDTO>> getDutiesForToday(
            @PathVariable String orgId) {
        List<EmployeeDutyResponseDTO> duties = dutyService.getDutiesForToday(orgId);
        return ResponseEntity.ok(duties);
    }

    @GetMapping("/week")
    public ResponseEntity<List<EmployeeDutyResponseDTO>> getDutiesForWeek(
            @PathVariable String orgId) {
        List<EmployeeDutyResponseDTO> duties = dutyService.getDutiesForWeek(orgId);
        return ResponseEntity.ok(duties);
    }

    @GetMapping("/month")
    public ResponseEntity<List<EmployeeDutyResponseDTO>> getDutiesForMonth(
            @PathVariable String orgId) {
        List<EmployeeDutyResponseDTO> duties = dutyService.getDutiesForMonth(orgId);
        return ResponseEntity.ok(duties);
    }

    @GetMapping("/custom-date")
    public ResponseEntity<List<EmployeeDutyResponseDTO>> getDutiesByCustomDate(
            @PathVariable String orgId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<EmployeeDutyResponseDTO> duties = dutyService.getDutiesByCustomDate(orgId, date);
        return ResponseEntity.ok(duties);
    }

    @GetMapping("/custom-range")
    public ResponseEntity<List<EmployeeDutyResponseDTO>> getDutiesByCustomRange(
            @PathVariable String orgId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<EmployeeDutyResponseDTO> duties = dutyService.getDutiesByCustomRange(orgId, startDate, endDate);
        return ResponseEntity.ok(duties);
    }
}
