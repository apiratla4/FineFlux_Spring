package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.SaleHistoryResponseDTO;
import com.pulse.fineflux.entity.SaleHistory;
import com.pulse.fineflux.repository.SaleHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
        // Assume h.getDateTime() is stored in UTC (recommended) and convert once to IST for UI [web:22][web:215]
        LocalDateTime utcStored = h.getDateTime(); // UTC in DB [web:215]
        ZonedDateTime istZdt = null;
        if (utcStored != null) {
            istZdt = utcStored
                    .atZone(ZoneId.of("UTC"))
                    .withZoneSameInstant(ZoneId.of("Asia/Kolkata")); // preserve instant, render in IST [web:22]
        }

        // Build response: dateTime=IST LocalDateTime (for UI grids), dateTimeString=ISO string with +05:30 [web:22]
        return SaleHistoryResponseDTO.builder()
                .id(h.getId())
                .organizationId(h.getOrganizationId())
                .dateTime(istZdt != null ? istZdt.toLocalDateTime() : null) // IST local date-time for UI [web:22]
                .dateTimeString(istZdt != null ? istZdt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null) // ISO with +05:30 [web:221]
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
