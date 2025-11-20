package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.SaleHistoryResponseDTO;
import com.pulse.fineflux.entity.SaleHistory;
import com.pulse.fineflux.repository.SaleHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaleHistoryServiceImpl implements SaleHistoryService {

    private final SaleHistoryRepository saleHistoryRepository;

    @Override
    public List<SaleHistoryResponseDTO> getAll(String orgId) {
        log.info("Fetching all SaleHistory records for orgId={} in ascending order", orgId);
        return saleHistoryRepository.findByOrganizationIdOrderByDateTimeAsc(orgId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SaleHistoryResponseDTO> getByDateRange(String orgId, LocalDateTime from, LocalDateTime to) {
        log.info("Fetching SaleHistory by date range for orgId={}, from={}, to={} in DESC order", orgId, from, to);
        return saleHistoryRepository.findByOrganizationIdAndDateTimeBetweenOrderByDateTimeAsc(orgId, from, to)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private SaleHistoryResponseDTO toResponse(SaleHistory h) {
        return SaleHistoryResponseDTO.builder()
                .id(h.getId())
                .organizationId(h.getOrganizationId())
                .dateTime(h.getDateTime())
                .dateTimeString(h.getDateTime() != null ? h.getDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null)
                .productName(h.getProductName())
                .guns(h.getGuns())
                .empId(h.getEmpId())
                .openingStock(h.getOpeningStock())
                .closingStock(h.getClosingStock())
                .testingTotal(h.getTestingTotal())
                .salesInLiters(h.getSalesInLiters())
                .price(h.getPrice())
                .salesInRupees(h.getSalesInRupees())
                .cashReceived(h.getCashReceived())
                .phonePay(h.getPhonePay())
                .creditCard(h.getCreditCard())
                .shortCollections(h.getShortCollections())
                .receivedTotal(h.getReceivedTotal())
                .build();
    }
}
