package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.EmployeeAttendanceCreateDTO;
import com.pulse.fineflux.domain.EmployeeAttendanceResponseDTO;
import com.pulse.fineflux.domain.EmployeeAttendanceUpdateDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface EmployeeAttendanceService {
    EmployeeAttendanceResponseDTO create(EmployeeAttendanceCreateDTO dto);
    EmployeeAttendanceResponseDTO update(String id, EmployeeAttendanceUpdateDTO dto);
    void delete(String id);
    EmployeeAttendanceResponseDTO getById(String id);
    List<EmployeeAttendanceResponseDTO> getAll();
    List<EmployeeAttendanceResponseDTO> getByEmpId(String empId);
    List<EmployeeAttendanceResponseDTO> getByEmpIdOneDay(String empId, LocalDateTime day);
    List<EmployeeAttendanceResponseDTO> getByEmpIdOneWeek(String empId, LocalDateTime reference);
    List<EmployeeAttendanceResponseDTO> getByEmpIdOneMonth(String empId, LocalDateTime reference);
    List<EmployeeAttendanceResponseDTO> getByEmpIdThreeMonths(String empId, LocalDateTime reference);
    List<EmployeeAttendanceResponseDTO> getByEmpIdSixMonths(String empId, LocalDateTime reference);
}
