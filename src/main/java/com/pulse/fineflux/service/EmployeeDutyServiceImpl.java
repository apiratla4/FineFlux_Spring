// service/EmployeeDutyServiceImpl.java
package com.pulse.fineflux.service;

import com.pulse.fineflux.entity.EmployeeDuty;
import com.pulse.fineflux.domain.EmployeeDutyCreateDTO;
import com.pulse.fineflux.domain.EmployeeDutyResponseDTO;
import com.pulse.fineflux.domain.EmployeeDutyUpdateDTO;
import com.pulse.fineflux.repository.EmployeeDutyRepository;
import com.pulse.fineflux.repository.ProductRepository;
import com.pulse.fineflux.repository.GunInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmployeeDutyServiceImpl implements EmployeeDutyService {

    @Autowired
    private EmployeeDutyRepository dutyRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private GunInfoRepository gunInfoRepository;

    // ==================== CRUD OPERATIONS ====================

    @Override
    public EmployeeDutyResponseDTO createDuty(EmployeeDutyCreateDTO createDTO) {
        Optional<EmployeeDuty> existing = dutyRepository.findByOrganizationIdAndEmpIdAndDutyDate(
                createDTO.getOrganizationId(),
                createDTO.getEmpId(),
                createDTO.getDutyDate()
        );

        if (existing.isPresent()) {
            throw new RuntimeException("Duty already exists for this employee on this date");
        }

        for (String productName : createDTO.getProductNames()) {
            boolean productExists = productRepository.existsByOrganizationIdAndProductName(
                    createDTO.getOrganizationId(),
                    productName
            );
            if (!productExists) {
                throw new RuntimeException("Product not found: " + productName);
            }
        }

        for (String gunName : createDTO.getGunNames()) {
            boolean gunExists = gunInfoRepository.existsByOrganizationIdAndGuns(
                    createDTO.getOrganizationId(),
                    gunName
            );
            if (!gunExists) {
                throw new RuntimeException("Gun not found: " + gunName);
            }
        }

        EmployeeDuty duty = new EmployeeDuty();
        duty.setOrganizationId(createDTO.getOrganizationId());
        duty.setEmpId(createDTO.getEmpId());
        duty.setDutyDate(createDTO.getDutyDate());
        duty.setProductNames(createDTO.getProductNames());
        duty.setGunNames(createDTO.getGunNames());
        duty.setShiftStart(createDTO.getShiftStart());
        duty.setShiftEnd(createDTO.getShiftEnd());
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

        if (updateDTO.getProductNames() != null && !updateDTO.getProductNames().isEmpty()) {
            for (String productName : updateDTO.getProductNames()) {
                boolean productExists = productRepository.existsByOrganizationIdAndProductName(
                        duty.getOrganizationId(),
                        productName
                );
                if (!productExists) {
                    throw new RuntimeException("Product not found: " + productName);
                }
            }
            duty.setProductNames(updateDTO.getProductNames());
        }

        if (updateDTO.getGunNames() != null && !updateDTO.getGunNames().isEmpty()) {
            for (String gunName : updateDTO.getGunNames()) {
                boolean gunExists = gunInfoRepository.existsByOrganizationIdAndGuns(
                        duty.getOrganizationId(),
                        gunName
                );
                if (!gunExists) {
                    throw new RuntimeException("Gun not found: " + gunName);
                }
            }
            duty.setGunNames(updateDTO.getGunNames());
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

    // ==================== BASIC QUERY OPERATIONS ====================

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

    @Override
    public List<EmployeeDutyResponseDTO> getDutiesByDate(String organizationId, LocalDate date) {
        return dutyRepository.findByOrganizationIdAndDutyDate(organizationId, date).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeDutyResponseDTO> getDutiesByDateRangeAndStatus(
            String organizationId, LocalDate startDate, LocalDate endDate, String status) {
        return dutyRepository.findByOrganizationIdAndDutyDateBetweenAndStatus(
                        organizationId, startDate, endDate, status).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // ==================== DATE-BASED QUERIES FOR ORGANIZATION ====================

    @Override
    public List<EmployeeDutyResponseDTO> getDutiesForToday(String organizationId) {
        LocalDate today = LocalDate.now();
        return dutyRepository.findByOrganizationIdAndDutyDate(organizationId, today).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeDutyResponseDTO> getDutiesForThisWeek(String organizationId) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);

        return dutyRepository.findByOrganizationIdAndDutyDateBetween(
                        organizationId, startOfWeek, endOfWeek).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeDutyResponseDTO> getDutiesForThisMonth(String organizationId) {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());

        return dutyRepository.findByOrganizationIdAndDutyDateBetween(
                        organizationId, startOfMonth, endOfMonth).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeDutyResponseDTO> getDutiesByCustomDateRange(
            String organizationId, LocalDate startDate, LocalDate endDate) {
        return dutyRepository.findByOrganizationIdAndDutyDateBetween(
                        organizationId, startDate, endDate).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // ==================== DATE-BASED QUERIES FOR SPECIFIC EMPLOYEE ====================

    @Override
    public List<EmployeeDutyResponseDTO> getEmployeeDutiesForToday(String organizationId, String empId) {
        LocalDate today = LocalDate.now();
        return dutyRepository.findByOrganizationIdAndEmpIdAndDutyDateBetween(
                        organizationId, empId, today, today).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeDutyResponseDTO> getEmployeeDutiesForThisWeek(String organizationId, String empId) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);

        return dutyRepository.findByOrganizationIdAndEmpIdAndDutyDateBetween(
                        organizationId, empId, startOfWeek, endOfWeek).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeDutyResponseDTO> getEmployeeDutiesForThisMonth(String organizationId, String empId) {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());

        return dutyRepository.findByOrganizationIdAndEmpIdAndDutyDateBetween(
                        organizationId, empId, startOfMonth, endOfMonth).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeDutyResponseDTO> getEmployeeDutiesByCustomDateRange(
            String organizationId, String empId, LocalDate startDate, LocalDate endDate) {
        return dutyRepository.findByOrganizationIdAndEmpIdAndDutyDateBetween(
                        organizationId, empId, startDate, endDate).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // ==================== HELPER METHOD ====================

    private EmployeeDutyResponseDTO convertToResponseDTO(EmployeeDuty duty) {
        EmployeeDutyResponseDTO responseDTO = new EmployeeDutyResponseDTO();

        responseDTO.setId(duty.getId());
        responseDTO.setOrganizationId(duty.getOrganizationId());
        responseDTO.setEmpId(duty.getEmpId());
        responseDTO.setDutyDate(duty.getDutyDate());
        responseDTO.setProductNames(duty.getProductNames());
        responseDTO.setGunNames(duty.getGunNames());
        responseDTO.setShiftStart(duty.getShiftStart());
        responseDTO.setShiftEnd(duty.getShiftEnd());
        responseDTO.setTotalHours(duty.getTotalHours());
        responseDTO.setStatus(duty.getStatus());
        responseDTO.setCreatedAt(duty.getCreatedAt());
        responseDTO.setUpdatedAt(duty.getUpdatedAt());

        return responseDTO;
    }
}
