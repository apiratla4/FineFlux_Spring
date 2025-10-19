package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.EmployeeDutyCreateDTO;
import com.pulse.fineflux.domain.EmployeeDutyResponseDTO;
import com.pulse.fineflux.domain.EmployeeDutyUpdateDTO;

import java.time.LocalDate;
import java.util.List;

public interface EmployeeDutyService {

    EmployeeDutyResponseDTO createDuty(EmployeeDutyCreateDTO createDTO);

    EmployeeDutyResponseDTO updateDuty(String id, EmployeeDutyUpdateDTO updateDTO);

    void deleteDuty(String id);

    EmployeeDutyResponseDTO getDutyById(String id);

    List<EmployeeDutyResponseDTO> getAllDuties();

    List<EmployeeDutyResponseDTO> getDutiesByOrgId(String orgId);

    List<EmployeeDutyResponseDTO> getDutiesByEmpId(String empId);

    List<EmployeeDutyResponseDTO> getDutiesByOrgAndEmployee(String orgId, String empId);

    List<EmployeeDutyResponseDTO> getDutiesByEmployeeAndDateRange(String empId, LocalDate startDate, LocalDate endDate);

    List<EmployeeDutyResponseDTO> getDutiesByStatus(String orgId, String status);

    // New methods for time-based filtering
    List<EmployeeDutyResponseDTO> getDutiesForToday(String orgId);

    List<EmployeeDutyResponseDTO> getDutiesForWeek(String orgId);

    List<EmployeeDutyResponseDTO> getDutiesForMonth(String orgId);

    List<EmployeeDutyResponseDTO> getDutiesByCustomDate(String orgId, LocalDate date);

    List<EmployeeDutyResponseDTO> getDutiesByCustomRange(String orgId, LocalDate startDate, LocalDate endDate);
}
