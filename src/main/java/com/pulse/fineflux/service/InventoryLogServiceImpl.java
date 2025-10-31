package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.InventoryLogResponseDTO;
import com.pulse.fineflux.entity.InventoryLog;
import com.pulse.fineflux.repository.InventoryLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryLogServiceImpl implements InventoryLogService {

    private final InventoryLogRepository inventoryLogRepository;

    private static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");

    @Override
    public List<InventoryLogResponseDTO> getAllLogs(String orgId) {
        // Fetch ALL logs, sort DESC by IST lastUpdated (recent top), map to IST
        List<InventoryLog> logs = inventoryLogRepository.findByOrganizationId(orgId);
        return logs.stream()
                .sorted(Comparator.comparing(InventoryLog::getLastUpdated).reversed()) // Most recent first
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public InventoryLogResponseDTO getLogById(String orgId, String id) {
        InventoryLog log = inventoryLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Log not found"));
        return toDto(log);
    }

    @Override
    public List<InventoryLogResponseDTO> getLogsByProductName(String orgId, String productName) {
        String regex = "^" + productName.trim() + "\\s*$";
        List<InventoryLog> logs = inventoryLogRepository.findByOrganizationIdAndProductNameRegex(orgId, regex);
        return logs.stream()
                .sorted(Comparator.comparing(InventoryLog::getLastUpdated).reversed())
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteLog(String orgId, String id) {
        InventoryLog log = inventoryLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Log not found: " + id));
        if (!log.getOrganizationId().equals(orgId)) {
            throw new RuntimeException("Log does not belong to this organization");
        }
        inventoryLogRepository.deleteById(id);
    }

    // Internal mapping helper: Always map lastUpdated to IST for DTO
    private InventoryLogResponseDTO toDto(InventoryLog log) {
        LocalDateTime lastUpdated = log.getLastUpdated();
        LocalDateTime istTime = lastUpdated == null ? null
                : lastUpdated.atZone(ZoneId.systemDefault())
                .withZoneSameInstant(IST_ZONE)
                .toLocalDateTime();
        return InventoryLogResponseDTO.builder()
                .id(log.getId())
                .inventoryId(log.getInventoryId())
                .organizationId(log.getOrganizationId())
                .productId(log.getProductId())
                .productName(log.getProductName())
                .totalCapacity(log.getTotalCapacity())
                .stockValue(log.getStockValue())
                .lastUpdated(istTime) // Always send IST to UI
                .empId(log.getEmpId())
                .currentLevel(log.getCurrentLevel())
                .metric(log.getMetric())
                .status(log.getStatus())
                .tankCapacity(log.getTankCapacity())
                .receiptQuantityInLitres(log.getReceiptQuantityInLitres())
                .mutationby(log.getMutationby())
                .build();
    }
}
