package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.FinanceSummaryCreateDTO;
import com.pulse.fineflux.domain.FinanceSummaryUpdateDTO;
import com.pulse.fineflux.domain.FinanceSummaryResponseDTO;
import java.util.List;

public interface FinanceSummaryService {
    FinanceSummaryResponseDTO update(String id, FinanceSummaryUpdateDTO dto);
    FinanceSummaryResponseDTO getLatestByOrg(String orgId);
    List<FinanceSummaryResponseDTO> getAllByOrg(String orgId);
    // Core business logic: auto-calculate/aggregate and create summary for org
    FinanceSummaryResponseDTO autoCreateFinanceSummary(String orgId);
}
