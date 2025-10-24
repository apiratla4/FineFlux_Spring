package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.*;
import com.pulse.fineflux.entity.DensityRegister;
import com.pulse.fineflux.repository.DensityRegisterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DensityRegisterServiceImpl implements DensityRegisterService {

    private final DensityRegisterRepository repository;

    @Override
    public DensityRegisterResponseDTO create(DensityRegisterDTO dto) {
        try {
            log.info("Creating DensityRegister for org: {}", dto.getOrganizationId());
            DensityRegister entity = new DensityRegister();
            BeanUtils.copyProperties(dto, entity);

            entity.setConvertedToCelsius(calcConvertedToCelsius(dto.getHydramMeter(), dto.getTempaturInCelsius()));
            entity.setDifference(roundTo2(dto.getCompositeReading() - dto.getAsPerChallan()));

            DensityRegister saved = repository.save(entity);
            log.info("Successfully created DensityRegister with id: {}", saved.getId());
            return convertToResponse(saved);
        } catch (Exception e) {
            log.error("Error creating DensityRegister: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create density register entry", e);
        }
    }

    @Override
    public DensityRegisterResponseDTO update(String id, String orgId, DensityRegisterUpdateDTO dto) {
        try {
            log.info("Updating DensityRegister id: {} for orgId: {}", id, orgId);
            DensityRegister entity = repository.findById(id)
                    .filter(sr -> sr.getOrganizationId().equals(orgId))
                    .orElseThrow(() -> new RuntimeException("DensityRegister entry not found: " + id + " for orgId: " + orgId));
            BeanUtils.copyProperties(dto, entity, "id", "organizationId");

            entity.setConvertedToCelsius(calcConvertedToCelsius(dto.getHydramMeter(), dto.getTempaturInCelsius()));
            entity.setDifference(roundTo2(dto.getCompositeReading() - dto.getAsPerChallan()));

            DensityRegister saved = repository.save(entity);
            log.info("Successfully updated DensityRegister with id: {}", saved.getId());
            return convertToResponse(saved);
        } catch (Exception e) {
            log.error("Error updating DensityRegister: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update density register entry", e);
        }
    }

    @Override
    public DensityRegisterResponseDTO getByIdForOrg(String id, String orgId) {
        try {
            log.info("Fetching DensityRegister by id={} and orgId={}", id, orgId);
            DensityRegister entity = repository.findById(id)
                    .filter(sr -> sr.getOrganizationId().equals(orgId))
                    .orElseThrow(() -> new RuntimeException("DensityRegister entry not found: " + id + " for orgId: " + orgId));
            return convertToResponse(entity);
        } catch (Exception e) {
            log.error("Error fetching DensityRegister: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch density register entry", e);
        }
    }

    @Override
    public List<DensityRegisterResponseDTO> getByOrganizationId(String orgId) {
        try {
            log.info("Fetching DensityRegister by OrganizationId: {}", orgId);
            List<DensityRegister> entities = repository.findByOrganizationId(orgId);
            log.info("Found {} DensityRegister entries for orgId: {}", entities.size(), orgId);
            return entities.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching DensityRegister list by orgId: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch density register list by orgId", e);
        }
    }

    @Override
    public void delete(String id, String orgId) {
        try {
            log.info("Deleting DensityRegister id={} orgId={}", id, orgId);
            DensityRegister entity = repository.findById(id)
                    .filter(sr -> sr.getOrganizationId().equals(orgId))
                    .orElseThrow(() -> new RuntimeException("DensityRegister entry not found: " + id + " for orgId: " + orgId));
            repository.deleteById(id);
            log.info("Successfully deleted DensityRegister with id: {}", id);
        } catch (Exception e) {
            log.error("Error deleting DensityRegister: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete density register entry", e);
        }
    }

    private DensityRegisterResponseDTO convertToResponse(DensityRegister entity) {
        DensityRegisterResponseDTO dto = new DensityRegisterResponseDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    // Final formula: HydramMeter + (TempaturInCelsius × 0.415), rounded to two decimals
    private double calcConvertedToCelsius(double hydramMeter, double tempatur) {
        double result = hydramMeter + (tempatur * 0.415);
        log.debug("Calculated ConvertedToCelsius: {} + ({} × 0.415) = {}", hydramMeter, tempatur, result);
        return roundTo2(result);
    }

    private double roundTo2(double val) {
        return BigDecimal.valueOf(val).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
