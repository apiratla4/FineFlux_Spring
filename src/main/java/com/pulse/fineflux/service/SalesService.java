package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.*;
import java.util.List;

public interface SalesService {
    SalesResponseDTO createSale(SalesCreateDTO dto);

    SalesResponseDTO updateSale(String id, SalesUpdateDTO dto);

    void deleteSale(String id);

    SalesResponseDTO getSaleById(String id);

    List<SalesResponseDTO> getAllSales(String organizationId);
}
