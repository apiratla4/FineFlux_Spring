package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.*;

import java.time.LocalDateTime;
import java.util.List;

public interface SalesService {
    SalesResponseDTO createSale(SalesCreateDTO dto);
    SalesResponseDTO updateSale(String id, SalesUpdateDTO dto);
    SalesResponseDTO getSaleById(String id);
    List<SalesResponseDTO> getAllSales(String organizationId);
    List<SalesResponseDTO> getSalesByDateRange(String organizationId, LocalDateTime from, LocalDateTime to);
 void deleteSale(String saleId, String employeeId);

}
