package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.StockRegisterDTO;
import com.pulse.fineflux.domain.StockRegisterResponseDTO;
import com.pulse.fineflux.entity.StockRegister;
import com.pulse.fineflux.repository.StockRegisterRepository;
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
public class StockRegisterServiceImpl implements StockRegisterService {

    private final StockRegisterRepository repository;

    @Override
    public StockRegisterResponseDTO create(StockRegisterDTO dto) {
        try {
            log.info("Creating StockRegister for org: {}", dto.getOrganizationId());
            StockRegister entity = new StockRegister();
            BeanUtils.copyProperties(dto, entity);

            double var = dto.getActualSalesAsPerMeter() - dto.getSaleAsForTankStock();
            entity.setStockVariation(BigDecimal.valueOf(var).setScale(2, RoundingMode.HALF_UP).doubleValue());
            StockRegister saved = repository.save(entity);
            return convertToResponse(saved);
        } catch (Exception e) {
            log.error("Error creating StockRegister: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create stock register entry", e);
        }
    }

    @Override
    public StockRegisterResponseDTO update(String id, String orgId, StockRegisterDTO dto) {
        try {
            log.info("Updating StockRegister id: {} for orgId={}", id, orgId);
            StockRegister entity = repository.findById(id)
                    .filter(sr -> sr.getOrganizationId().equals(orgId))
                    .orElseThrow(() -> new RuntimeException("StockRegister entry not found: " + id + " for orgId: " + orgId));
            BeanUtils.copyProperties(dto, entity, "id", "organizationId");

            double var = dto.getActualSalesAsPerMeter() - dto.getSaleAsForTankStock();
            entity.setStockVariation(BigDecimal.valueOf(var).setScale(2, RoundingMode.HALF_UP).doubleValue());
            StockRegister saved = repository.save(entity);
            return convertToResponse(saved);
        } catch (Exception e) {
            log.error("Error updating StockRegister: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update stock register entry", e);
        }
    }

    @Override
    public StockRegisterResponseDTO getByIdForOrg(String id, String orgId) {
        try {
            log.info("Fetching StockRegister by id={} and orgId={}", id, orgId);
            StockRegister entity = repository.findById(id)
                    .filter(sr -> sr.getOrganizationId().equals(orgId))
                    .orElseThrow(() -> new RuntimeException("StockRegister entry not found: " + id + " for orgId: " + orgId));
            return convertToResponse(entity);
        } catch (Exception e) {
            log.error("Error fetching StockRegister: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch stock register entry", e);
        }
    }

    @Override
    public List<StockRegisterResponseDTO> getByOrganizationId(String orgId) {
        try {
            log.info("Fetching StockRegister by OrganizationId: {}", orgId);
            return repository.findByOrganizationId(orgId)
                    .stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching StockRegister list by orgId: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch stock register list by orgId", e);
        }
    }

    @Override
    public void delete(String id, String orgId) {
        try {
            log.info("Deleting StockRegister id={} orgId={}", id, orgId);
            StockRegister entity = repository.findById(id)
                    .filter(sr -> sr.getOrganizationId().equals(orgId))
                    .orElseThrow(() -> new RuntimeException("StockRegister entry not found: " + id + " for orgId: " + orgId));
            repository.deleteById(id);
        } catch (Exception e) {
            log.error("Error deleting StockRegister: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete stock register entry", e);
        }
    }

    private StockRegisterResponseDTO convertToResponse(StockRegister entity) {
        StockRegisterResponseDTO dto = new StockRegisterResponseDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
