package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.StockRegisterDTO;
import com.pulse.fineflux.domain.StockRegisterResponseDTO;
import java.util.List;

public interface StockRegisterService {
    StockRegisterResponseDTO create(StockRegisterDTO dto);
    StockRegisterResponseDTO update(String id, String orgId, StockRegisterDTO dto);
    StockRegisterResponseDTO getByIdForOrg(String id, String orgId);
    List<StockRegisterResponseDTO> getByOrganizationId(String orgId);
    void delete(String id, String orgId);
}
