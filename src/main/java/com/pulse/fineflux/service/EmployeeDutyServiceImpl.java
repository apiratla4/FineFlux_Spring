package com.pulse.fineflux.service;

import com.pulse.fineflux.entity.EmployeeDuty;
import com.pulse.fineflux.domain.EmployeeDutyCreateDTO;
import com.pulse.fineflux.domain.EmployeeDutyResponseDTO;
import com.pulse.fineflux.domain.EmployeeDutyUpdateDTO;
import com.pulse.fineflux.repository.EmployeeDutyRepository;
import com.pulse.fineflux.service.EmployeeDutyService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmployeeDutyServiceImpl implements EmployeeDutyService {

    @Autowired
    private EmployeeDutyRepository dutyRepository;

    @Override
    public EmployeeDutyResponseDTO createDuty(EmployeeDutyCreateDTO createDTO) {
        // Check if duty already exists
        Optional<EmployeeDuty> existing = dutyRepository.findByOrganizationIdAndEmpIdAndDutyDate(
                createDTO.getOrganizationId(),
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
        if (updateDTO.getProductIds() != null) {
            duty.setProductIds(updateDTO.getProductIds());
        }
        if (updateDTO.getGunIds() != null) {
            duty.setGunIds(updateDTO.getGunIds());
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
    public List<EmployeeDutyResponseDTO> getDutiesByOrganizationId(String organizationId) {
        return dutyRepository.findByOrganizationId(organizationId).stream()
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
    public List<EmployeeDutyResponseDTO> getDutiesByOrganizationAndEmployee(String organizationId, String empId) {
        return dutyRepository.findByOrganizationIdAndEmpId(organizationId, empId).stream()
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
    public List<EmployeeDutyResponseDTO> getDutiesByStatus(String organizationId, String status) {
        return dutyRepository.findByOrganizationIdAndStatus(organizationId, status).stream()
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
