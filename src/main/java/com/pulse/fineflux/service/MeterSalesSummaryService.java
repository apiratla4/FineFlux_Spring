package com.pulse.fineflux.service;


import com.pulse.fineflux.domain.MeterSalesDayDynamicDTO;

import java.util.List;

public interface MeterSalesSummaryService {
    List<MeterSalesDayDynamicDTO> getMeterSalesSummary(String orgId, String product);
}
