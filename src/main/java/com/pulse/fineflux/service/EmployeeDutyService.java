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

    List<EmployeeDutyResponseDTO> getDutiesByOrganizationId(String organizationId);

    List<EmployeeDutyResponseDTO> getDutiesByEmpId(String empId);

    List<EmployeeDutyResponseDTO> getDutiesByOrganizationAndEmployee(String organizationId, String empId);

    List<EmployeeDutyResponseDTO> getDutiesByEmployeeAndDateRange(String empId, LocalDate startDate, LocalDate endDate);

    List<EmployeeDutyResponseDTO> getDutiesByStatus(String organizationId, String status);
}
