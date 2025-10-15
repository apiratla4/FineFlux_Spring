package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.ExpenseCategoryCreateDTO;
import com.pulse.fineflux.domain.ExpenseCategoryUpdateDTO;
import com.pulse.fineflux.domain.ExpenseCategoryResponseDTO;

import java.util.List;

public interface ExpenseCategoryService {
    ExpenseCategoryResponseDTO create(ExpenseCategoryCreateDTO dto);
    ExpenseCategoryResponseDTO update(String id, ExpenseCategoryUpdateDTO dto);
    void deleteByOrg(String id, String organizationId);
    ExpenseCategoryResponseDTO getByOrgAndId(String organizationId, String id);
    List<ExpenseCategoryResponseDTO> getAllByOrg(String organizationId);
}
