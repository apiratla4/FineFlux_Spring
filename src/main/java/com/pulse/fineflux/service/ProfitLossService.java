package com.pulse.fineflux.service;


import com.pulse.fineflux.domain.ProfitLossResponseDTO;

import java.util.List;

public interface ProfitLossService {

    ProfitLossResponseDTO calculateAndSaveProfitLoss(String orgId);

    List<ProfitLossResponseDTO> getAllProfitLoss();
}
