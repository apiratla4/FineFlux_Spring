package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.ExpenseCreateDTO;
import com.pulse.fineflux.domain.ExpenseUpdateDTO;
import com.pulse.fineflux.domain.ExpenseResponseDTO;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseService {
    ExpenseResponseDTO create(ExpenseCreateDTO dto);
    ExpenseResponseDTO update(String id, ExpenseUpdateDTO dto);
    void deleteByOrg(String id, String organizationId);
    ExpenseResponseDTO getByOrgAndId(String organizationId, String id);
    List<ExpenseResponseDTO> getAllByOrg(String organizationId);


    List<ExpenseResponseDTO> searchByEmployeeName(String organizationId, String employeeName);
    List<ExpenseResponseDTO> searchByCategory(String organizationId, String categoryName);
    List<String> getAllEmployeeNames(String organizationId);
    List<ExpenseResponseDTO> searchByExpenseDateRange(String orgId, LocalDate from, LocalDate to);
}
