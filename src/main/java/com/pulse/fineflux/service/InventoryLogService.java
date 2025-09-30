package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.InventoryLogCreateDTO;
import com.pulse.fineflux.domain.InventoryLogResponseDTO;
import com.pulse.fineflux.domain.InventoryLogUpdateDTO;
import com.pulse.fineflux.entity.InventoryLog;
import com.pulse.fineflux.repository.InventoryLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryLogService {

    private final InventoryLogRepository inventoryLogRepository;

    /**
     * Create a new inventory log entry
     */
    @Transactional
    public InventoryLogResponseDTO createLog(InventoryLogCreateDTO dto) {
        log.info("Creating inventory log for productId={}", dto.getProductId());
        try {
            InventoryLog logEntry = InventoryLog.builder()
                    .productId(dto.getProductId())
                    .productName(dto.getProductName())
                    .quantity(dto.getQuantity())
                    .previousLevel(dto.getPreviousLevel())
                    .newLevel(dto.getCurrentLevel())
                    .currentLevel(dto.getCurrentLevel())
                    .metric(dto.getMetric())
                    .employeeId(dto.getEmployeeId())
                    .transactionDate(new Date())
                    .action(dto.getAction())
                    .build();

            InventoryLog savedLog = inventoryLogRepository.save(logEntry);
            log.info("Inventory log created: id={}, productId={}", savedLog.getId(), savedLog.getProductId());

            return mapToResponseDTO(savedLog);

        } catch (Exception e) {
            log.error("Failed to create inventory log for productId={}: {}", dto.getProductId(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Update an existing inventory log by ID
     */
    @Transactional
    public InventoryLogResponseDTO updateLog(String id, InventoryLogUpdateDTO dto) {
        log.info("Updating inventory log id={}", id);
        try {
            InventoryLog logEntry = inventoryLogRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Inventory log not found: " + id));

            logEntry.setQuantity(dto.getQuantity());
            logEntry.setCurrentLevel(dto.getCurrentLevel());
            logEntry.setMetric(dto.getMetric());
            logEntry.setEmployeeId(dto.getEmployeeId());
            logEntry.setAction(dto.getAction());
            logEntry.setTransactionDate(new Date());

            InventoryLog updatedLog = inventoryLogRepository.save(logEntry);
            log.info("Inventory log updated successfully: id={}", updatedLog.getId());

            return mapToResponseDTO(updatedLog);

        } catch (Exception e) {
            log.error("Failed to update inventory log id={}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Fetch all inventory logs
     */
    public List<InventoryLogResponseDTO> getAllLogs() {
        log.info("Fetching all inventory logs");
        try {
            return inventoryLogRepository.findAll().stream()
                    .map(this::mapToResponseDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to fetch all inventory logs: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Fetch a single inventory log by ID
     */
    public InventoryLogResponseDTO getLogById(String id) {
        log.info("Fetching inventory log by id={}", id);
        try {
            InventoryLog logEntry = inventoryLogRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Inventory log not found: " + id));
            return mapToResponseDTO(logEntry);
        } catch (Exception e) {
            log.error("Failed to fetch inventory log id={}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Search inventory logs by product name and/or date range
     */
    public List<InventoryLogResponseDTO> searchLogs(String productName, Date fromDate, Date toDate) {
        log.info("Searching inventory logs with productName={}, fromDate={}, toDate={}", productName, fromDate, toDate);
        try {
            List<InventoryLog> logs;

            // Apply filters dynamically based on parameters
            if (productName != null && fromDate != null && toDate != null) {
                logs = inventoryLogRepository.findByProductNameContainingIgnoreCaseAndTransactionDateBetween(
                        productName, fromDate, toDate);
            } else if (productName != null) {
                logs = inventoryLogRepository.findByProductNameContainingIgnoreCase(productName);
            } else if (fromDate != null && toDate != null) {
                logs = inventoryLogRepository.findByTransactionDateBetween(fromDate, toDate);
            } else {
                logs = inventoryLogRepository.findAll();
            }

            return logs.stream()
                    .map(this::mapToResponseDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Failed to search inventory logs: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Delete an inventory log by ID
     */
    @Transactional
    public void deleteLog(String id) {
        log.info("Deleting inventory log id={}", id);
        try {
            if (!inventoryLogRepository.existsById(id)) {
                log.warn("Inventory log not found for deletion: id={}", id);
                throw new RuntimeException("Inventory log not found: " + id);
            }
            inventoryLogRepository.deleteById(id);
            log.info("Inventory log deleted successfully: id={}", id);
        } catch (Exception e) {
            log.error("Failed to delete inventory log id={}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Map InventoryLog entity to InventoryLogResponseDTO
     */
    private InventoryLogResponseDTO mapToResponseDTO(InventoryLog log) {
        return InventoryLogResponseDTO.builder()
                .id(log.getId())
                .productId(log.getProductId())
                .productName(log.getProductName())
                .quantity(log.getQuantity())
                .previousLevel(log.getPreviousLevel())
                .newLevel(log.getNewLevel())
                .currentLevel(log.getCurrentLevel())
                .metric(log.getMetric())
                .employeeId(log.getEmployeeId())
                .transactionDate(log.getTransactionDate())
                .action(log.getAction())
                .build();
    }
}
