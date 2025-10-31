package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.InventoryLogResponseDTO;
import com.pulse.fineflux.entity.InventoryLog;
import com.pulse.fineflux.repository.InventoryLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryLogServiceImpl implements InventoryLogService {

    private final InventoryLogRepository inventoryLogRepository;

    @Override
    public List<InventoryLogResponseDTO> getAllLogs(String orgId) {
        return inventoryLogRepository.findByOrganizationIdOrderByLastUpdatedAsc(orgId)
                .stream()
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
        return inventoryLogRepository
                .findByOrganizationIdAndProductNameRegex(orgId, regex)
                .stream()
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

    // Internal mapping helper
    private InventoryLogResponseDTO toDto(InventoryLog log) {
        return InventoryLogResponseDTO.builder()
                .id(log.getId())                      // <--- Ensure this line is present
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
                .receiptQuantityInLitres(log.getReceiptQuantityInLitres())
                .build();
    }

}
