package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.InventoryLogResponseDTO;
import java.util.Date;
import java.util.List;

public interface InventoryLogService {
    List<InventoryLogResponseDTO> getAllLogs(String orgId);
    InventoryLogResponseDTO getLogById(String orgId, String id);
    List<InventoryLogResponseDTO> getLogsByProductName(String orgId, String productName);
}

