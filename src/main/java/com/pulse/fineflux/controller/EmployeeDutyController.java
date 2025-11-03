package com.pulse.fineflux.controller;

import com.pulse.fineflux.domain.EmployeeDutyCreateDTO;
import com.pulse.fineflux.domain.EmployeeDutyResponseDTO;
import com.pulse.fineflux.domain.EmployeeDutyUpdateDTO;
import com.pulse.fineflux.service.EmployeeDutyService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;
@Slf4j
@RestController
@RequestMapping("/api/organizations/{organizationId}/employee-duties")

@CrossOrigin(origins = "*")
public class EmployeeDutyController {

    @Autowired
    private EmployeeDutyService dutyService;

    @PostMapping
    public ResponseEntity<EmployeeDutyResponseDTO> createDuty(
            @PathVariable("organizationId") String organizationId,
            @RequestBody EmployeeDutyCreateDTO dto) {
        try {
            dto.setOrganizationId(organizationId);
            EmployeeDutyResponseDTO result = dutyService.createDuty(dto);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error creating duty for orgId={}: {}", organizationId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }


    @GetMapping
    public ResponseEntity<List<EmployeeDutyResponseDTO>> getAllDutiesByOrganization(
            @PathVariable String organizationId) {
        List<EmployeeDutyResponseDTO> duties = dutyService.getDutiesByOrganizationId(organizationId);
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
            @PathVariable String organizationId,
            @PathVariable String empId) {
        List<EmployeeDutyResponseDTO> duties = dutyService.getDutiesByOrganizationAndEmployee(organizationId, empId);
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
            @PathVariable String organizationId,
            @PathVariable String status) {
        List<EmployeeDutyResponseDTO> duties = dutyService.getDutiesByStatus(organizationId, status);
        return ResponseEntity.ok(duties);
    }
}
