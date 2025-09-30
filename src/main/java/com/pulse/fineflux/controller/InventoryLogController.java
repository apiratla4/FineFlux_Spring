package com.pulse.fineflux.controller;

import com.pulse.fineflux.domain.InventoryLogDto;
import com.pulse.fineflux.entity.InventoryLog;
import com.pulse.fineflux.repository.InventoryLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inventory-logs")
@RequiredArgsConstructor
@Slf4j
public class InventoryLogController {

    private final InventoryLogRepository inventoryLogRepository;

    // Get all logs
    @GetMapping
    public ResponseEntity<List<InventoryLogDto>> getAllLogs() {
        try {
            List<InventoryLogDto> logs = inventoryLogRepository.findAll()
                    .stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            log.error("Failed to fetch inventory logs: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    // Get logs for a specific product
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<InventoryLogDto>> getLogsByProduct(@PathVariable String productId) {
        try {
            List<InventoryLogDto> logs = inventoryLogRepository.findByProductIdOrderByTransactionDateDesc(productId)
                    .stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            log.error("Failed to fetch inventory logs for product {}: {}", productId, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    // Helper method to map InventoryLog → InventoryLogDto
    private InventoryLogDto toDto(InventoryLog log) {
        InventoryLogDto dto = new InventoryLogDto();
        dto.setId(log.getId());
        dto.setProductId(log.getProductId());
        dto.setQuantity(log.getQuantity());
        dto.setPreviousLevel(log.getPreviousLevel());
        dto.setNewLevel(log.getNewLevel());
        dto.setCurrentLevel(log.getCurrentLevel());
        dto.setMetric(log.getMetric());
        dto.setEmployeeId(log.getEmployeeId());
        dto.setTransactionDate(log.getTransactionDate());
        dto.setAction(log.getAction());
        return dto;
    }
}
