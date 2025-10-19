package com.pulse.fineflux.service;

import com.pulse.fineflux.entity.EmployeeDuty;
import com.pulse.fineflux.domain.EmployeeDutyCreateDTO;
import com.pulse.fineflux.domain.EmployeeDutyResponseDTO;
import com.pulse.fineflux.domain.EmployeeDutyUpdateDTO;
import com.pulse.fineflux.repository.EmployeeDutyRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.time.DayOfWeek;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmployeeDutyServiceImpl implements EmployeeDutyService {

    @Autowired
    private EmployeeDutyRepository dutyRepository;

    // IST Zone ID
    private static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");

    @Override
    public EmployeeDutyResponseDTO createDuty(EmployeeDutyCreateDTO createDTO) {
        // Check if duty already exists
        Optional<EmployeeDuty> existing = dutyRepository.findByOrgIdAndEmpIdAndDutyDate(
                createDTO.getOrgId(),
                createDTO.getEmpId(),
                createDTO.getDutyDate()
        );

        if (existing.isPresent()) {
            throw new RuntimeException("Duty already exists for this employee on this date");
        }

        EmployeeDuty duty = new EmployeeDuty();
        BeanUtils.copyProperties(createDTO, duty);

        duty.setStatus(createDTO.getStatus() != null ? createDTO.getStatus() : "SCHEDULED");
        duty.setCreatedAt(new Date());
        duty.setUpdatedAt(new Date());
        duty.calculateTotalHours();

        EmployeeDuty savedDuty = dutyRepository.save(duty);
        return convertToResponseDTO(savedDuty);
    }

    @Override
    public EmployeeDutyResponseDTO updateDuty(String id, EmployeeDutyUpdateDTO updateDTO) {
        EmployeeDuty duty = dutyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Duty not found with id: " + id));

        if (updateDTO.getDutyDate() != null) {
            duty.setDutyDate(updateDTO.getDutyDate());
        }
        if (updateDTO.getProducts() != null) {
            duty.setProducts(updateDTO.getProducts());
        }
        if (updateDTO.getGuns() != null) {
            duty.setGuns(updateDTO.getGuns());
        }
        if (updateDTO.getShiftStart() != null) {
            duty.setShiftStart(updateDTO.getShiftStart());
        }
        if (updateDTO.getShiftEnd() != null) {
            duty.setShiftEnd(updateDTO.getShiftEnd());
        }
        if (updateDTO.getStatus() != null) {
            duty.setStatus(updateDTO.getStatus());
        }

        duty.setUpdatedAt(new Date());
        duty.calculateTotalHours();

        EmployeeDuty updatedDuty = dutyRepository.save(duty);
        return convertToResponseDTO(updatedDuty);
    }

    @Override
    public void deleteDuty(String id) {
        if (!dutyRepository.existsById(id)) {
            throw new RuntimeException("Duty not found with id: " + id);
        }
        dutyRepository.deleteById(id);
    }

    @Override
    public EmployeeDutyResponseDTO getDutyById(String id) {
        EmployeeDuty duty = dutyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Duty not found with id: " + id));
        return convertToResponseDTO(duty);
    }

    @Override
    public List<EmployeeDutyResponseDTO> getAllDuties() {
        return dutyRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeDutyResponseDTO> getDutiesByOrgId(String orgId) {
        return dutyRepository.findByOrgId(orgId).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeDutyResponseDTO> getDutiesByEmpId(String empId) {
        return dutyRepository.findByEmpId(empId).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeDutyResponseDTO> getDutiesByOrgAndEmployee(String orgId, String empId) {
        return dutyRepository.findByOrgIdAndEmpId(orgId, empId).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeDutyResponseDTO> getDutiesByEmployeeAndDateRange(String empId, LocalDate startDate, LocalDate endDate) {
        return dutyRepository.findByEmpIdAndDutyDateBetween(empId, startDate, endDate).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeDutyResponseDTO> getDutiesByStatus(String orgId, String status) {
        return dutyRepository.findByOrgIdAndStatus(orgId, status).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // New time-based filtering methods
    @Override
    public List<EmployeeDutyResponseDTO> getDutiesForToday(String orgId) {
        LocalDate today = LocalDate.now(IST_ZONE);
        return dutyRepository.findByOrgIdAndDutyDate(orgId, today).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeDutyResponseDTO> getDutiesForWeek(String orgId) {
        LocalDate today = LocalDate.now(IST_ZONE);
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        return dutyRepository.findByOrgIdAndDutyDateBetween(orgId, startOfWeek, endOfWeek).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeDutyResponseDTO> getDutiesForMonth(String orgId) {
        LocalDate today = LocalDate.now(IST_ZONE);
        LocalDate startOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());

        return dutyRepository.findByOrgIdAndDutyDateBetween(orgId, startOfMonth, endOfMonth).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeDutyResponseDTO> getDutiesByCustomDate(String orgId, LocalDate date) {
        return dutyRepository.findByOrgIdAndDutyDate(orgId, date).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeDutyResponseDTO> getDutiesByCustomRange(String orgId, LocalDate startDate, LocalDate endDate) {
        return dutyRepository.findByOrgIdAndDutyDateBetween(orgId, startDate, endDate).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // Helper method to convert Entity to Response DTO
    private EmployeeDutyResponseDTO convertToResponseDTO(EmployeeDuty duty) {
        EmployeeDutyResponseDTO responseDTO = new EmployeeDutyResponseDTO();
        BeanUtils.copyProperties(duty, responseDTO);
        return responseDTO;
    }
}
