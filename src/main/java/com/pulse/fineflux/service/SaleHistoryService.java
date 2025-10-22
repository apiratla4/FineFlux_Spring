package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.SaleHistoryResponseDTO;
import java.time.LocalDateTime;
import java.util.List;

public interface SaleHistoryService {
    List<SaleHistoryResponseDTO> getAll(String orgId);
    List<SaleHistoryResponseDTO> getByDateRange(String orgId, LocalDateTime from, LocalDateTime to);
}
