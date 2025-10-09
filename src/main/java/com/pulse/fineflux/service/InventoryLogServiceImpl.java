package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.InventoryLogResponseDTO;
import com.pulse.fineflux.entity.InventoryLog;
import com.pulse.fineflux.repository.InventoryLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryLogServiceImpl implements InventoryLogService {

    private final InventoryLogRepository inventoryLogRepository;

    /**
     * Fetch all inventory logs for a given organization
     */
    @Override
    public List<InventoryLogResponseDTO> getAllLogs(String orgId) {
        try {
            log.info("Fetching all inventory logs for orgId={}", orgId);
            List<InventoryLogResponseDTO> logs = inventoryLogRepository.findByOrganizationId(orgId)
                    .stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
            log.debug("Fetched {} inventory logs for orgId={}", logs.size(), orgId);
            return logs;
        } catch (Exception e) {
            log.error("Error fetching inventory logs for orgId={}", orgId, e);
            throw e;
        }
    }

    /**
     * Fetch a single inventory log by inventoryId for a given organization
     */
    @Override
    public InventoryLogResponseDTO getLogById(String orgId, String inventoryId) {
        try {
            log.info("Fetching inventory log with inventoryId={} for orgId={}", inventoryId, orgId);
            return inventoryLogRepository.findByOrganizationIdAndInventoryId(orgId, inventoryId)
                    .map(this::toDto)
                    .orElseThrow(() -> new RuntimeException("Log not found"));
        } catch (Exception e) {
            log.error("Error fetching inventory log inventoryId={} for orgId={}", inventoryId, orgId, e);
            throw e;
        }
    }

    /**
     * Search inventory logs with optional filters for product name and date range
     */
    @Override
    public List<InventoryLogResponseDTO> searchLogs(String orgId, String productName, Date fromDate, Date toDate) {
        try {
            log.info("Searching inventory logs for orgId={} with filters productName={}, fromDate={}, toDate={}",
                    orgId, productName, fromDate, toDate);

            if (fromDate == null) fromDate = new Date(0); // epoch start
            if (toDate == null) {
                toDate = new Date(Long.MAX_VALUE); // far future
            } else {
                // Include the whole day for 'toDate'
                toDate = new Date(toDate.getTime() + 24 * 60 * 60 * 1000 - 1);
            }

            List<InventoryLogResponseDTO> logs = inventoryLogRepository
                    .findByOrganizationIdAndProductNameIgnoreCaseAndLastUpdatedBetween(
                            orgId, productName, fromDate, toDate)
                    .stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());

            log.debug("Found {} inventory logs for orgId={} with productName={}", logs.size(), orgId, productName);
            return logs;
        } catch (Exception e) {
            log.error("Error searching inventory logs for orgId={} with productName={}, fromDate={}, toDate={}",
                    orgId, productName, fromDate, toDate, e);
            throw e;
        }
    }

    /**
     * Convert InventoryLog entity to DTO
     */
    private InventoryLogResponseDTO toDto(InventoryLog log) {
        return InventoryLogResponseDTO.builder()
                .inventoryId(log.getInventoryId())
                .organizationId(log.getOrganizationId())
                .productId(log.getProductId())
                .productName(log.getProductName())
                .totalCapacity(log.getTotalCapacity())
                .stockValue(log.getStockValue())
                .lastUpdated(log.getLastUpdated())
                .empId(log.getEmpId())
                .currentLevel(log.getCurrentLevel())
                .metric(log.getMetric())
                .status(log.getStatus())
                .tankCapacity(log.getTankCapacity())
                .build();
    }
}
