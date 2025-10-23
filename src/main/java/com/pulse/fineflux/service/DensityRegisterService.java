package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.*;
import java.util.List;

public interface DensityRegisterService {
    DensityRegisterResponseDTO create(DensityRegisterDTO dto);
    DensityRegisterResponseDTO update(String id, String orgId, DensityRegisterUpdateDTO dto);
    DensityRegisterResponseDTO getByIdForOrg(String id, String orgId);
    List<DensityRegisterResponseDTO> getByOrganizationId(String orgId);
    void delete(String id, String orgId);
}
