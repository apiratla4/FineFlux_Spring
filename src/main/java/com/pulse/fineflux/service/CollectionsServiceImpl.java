package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.*;
import com.pulse.fineflux.entity.Collections;
import com.pulse.fineflux.entity.Sales;
import com.pulse.fineflux.entity.SaleHistory;
import com.pulse.fineflux.repository.CollectionsRepository;
import com.pulse.fineflux.repository.SalesRepository;
import com.pulse.fineflux.repository.SaleHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectionsServiceImpl implements CollectionsService {

    private final CollectionsRepository collectionsRepository;
    private final SalesRepository salesRepository;
    private final SaleHistoryRepository saleHistoryRepository;

    @Override
    public CollectionsResponseDTO create(CollectionsCreateDTO dto) {
        log.info("Creating collection for org: {}", dto.getOrganizationId());

        Collections entity = new Collections();
        BeanUtils.copyProperties(dto, entity);

        // Exact match first
        Sales matchingSale = salesRepository
                .findByOrganizationIdAndEmpIdAndProductNameAndGunsAndDateTime(
                        dto.getOrganizationId(),
                        dto.getEmpId(),
                        dto.getProductName(),
                        dto.getGuns(),
                        dto.getDateTime())
                .stream()
                .findFirst()
                .orElse(null);

        // Fallback: latest in day window
        if (matchingSale == null) {
            LocalDateTime dayStart = dto.getDateTime().toLocalDate().atStartOfDay();
            LocalDateTime dayEnd = dayStart.plusDays(1);

            matchingSale = salesRepository
                    .findByOrganizationIdAndEmpIdAndProductNameAndGunsAndDateTimeBetween(
                            dto.getOrganizationId(),
                            dto.getEmpId(),
                            dto.getProductName(),
                            dto.getGuns(),
                            dayStart, dayEnd)
                    .stream()
                    .max((a, b) -> b.getDateTime().compareTo(a.getDateTime()))
                    .orElse(null);
        }

        double expected = (matchingSale != null) ? matchingSale.getSalesInRupees() : 0.0;
        double received = dto.getCashReceived() + dto.getPhonePay() + dto.getCreditCard();
        entity.setExpectedTotal(expected);
        entity.setReceivedTotal(received);
        entity.setShortCollections(expected - received);

        // Save collection
        Collections saved = collectionsRepository.save(entity);

        // Day window
        LocalDateTime dayStart = dto.getDateTime().toLocalDate().atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);

        List<Sales> salesList = salesRepository.findByOrganizationIdAndEmpIdAndDateTimeBetween(
                saved.getOrganizationId(), saved.getEmpId(), dayStart, dayEnd);

        // Use collection's dateTime as the unique event key for SaleHistory
        LocalDateTime historyEventTime = saved.getDateTime();

        for (Sales sale : salesList) {
            // If a row for this sale+event already exists, skip
            boolean exists = !saleHistoryRepository
                    .findByOrganizationIdAndEmpIdAndProductNameAndGunsAndDateTime(
                            sale.getOrganizationId(),
                            sale.getEmpId(),
                            sale.getProductName(),
                            sale.getGuns(),
                            historyEventTime
                    ).isEmpty();
            if (exists) {
                log.info("SaleHistory already exists for orgId={} empId={} product={} guns={} at eventTime={}, skipping",
                        sale.getOrganizationId(), sale.getEmpId(), sale.getProductName(), sale.getGuns(), historyEventTime);
                continue;
            }

            // Create exactly one row for this collection event
            SaleHistory history = SaleHistory.builder()
                    .organizationId(sale.getOrganizationId())
                    .dateTime(historyEventTime) // event time = collection time
                    .productName(sale.getProductName())
                    .guns(sale.getGuns())
                    .empId(sale.getEmpId())
                    .openingStock(sale.getOpeningStock())
                    .closingStock(sale.getClosingStock())
                    .testingTotal(sale.getTestingTotal())
                    .salesInLiters(sale.getSalesInLiters())
                    .price(sale.getPrice())
                    .salesInRupees(sale.getSalesInRupees())
                    .cashReceived(saved.getCashReceived())
                    .phonePay(saved.getPhonePay())
                    .creditCard(saved.getCreditCard())
                    .shortCollections(saved.getShortCollections())
                    .receivedTotal(saved.getReceivedTotal())
                    .build();

            saleHistoryRepository.save(history);

            log.info("SaleHistory created (collection event) for saleId={} empId={} orgId={} eventTime={}",
                    sale.getId(), sale.getEmpId(), sale.getOrganizationId(), historyEventTime);
        }

        return convertToResponse(saved);
    }

    @Override
    public CollectionsResponseDTO update(String id, CollectionsUpdateDTO dto) {
        Collections existing = collectionsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Collection not found"));

        BeanUtils.copyProperties(dto, existing, "id", "organizationId");

        double received = dto.getCashReceived() + dto.getPhonePay() + dto.getCreditCard();
        existing.setReceivedTotal(received);

        Collections saved = collectionsRepository.save(existing);

        // --- Again update SaleHistory for all sales for this emp/org/date with this new collection info ---
        LocalDateTime dayStart = saved.getDateTime().toLocalDate().atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);

        List<Sales> salesList = salesRepository.findByOrganizationIdAndEmpIdAndDateTimeBetween(
                saved.getOrganizationId(), saved.getEmpId(), dayStart, dayEnd);

        for (Sales sale : salesList) {
            SaleHistory history = SaleHistory.builder()
                    .organizationId(sale.getOrganizationId())
                    .dateTime(sale.getDateTime())
                    .productName(sale.getProductName())
                    .guns(sale.getGuns())
                    .empId(sale.getEmpId())
                    .openingStock(sale.getOpeningStock())
                    .closingStock(sale.getClosingStock())
                    .testingTotal(sale.getTestingTotal())
                    .salesInLiters(sale.getSalesInLiters())
                    .price(sale.getPrice())
                    .salesInRupees(sale.getSalesInRupees())
                    .cashReceived(saved.getCashReceived())
                    .phonePay(saved.getPhonePay())
                    .creditCard(saved.getCreditCard())
                    .shortCollections(saved.getShortCollections())
                    .receivedTotal(saved.getReceivedTotal())
                    .build();
            saleHistoryRepository.save(history);
            log.info("SaleHistory updated (via collection update) for saleId={} empId={} orgId={}",
                    sale.getId(), sale.getEmpId(), sale.getOrganizationId());
        }

        return convertToResponse(saved);
    }

    @Override
    public CollectionsResponseDTO getById(String id) {
        Collections entity = collectionsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Collection not found"));
        return convertToResponse(entity);
    }

    @Override
    public List<CollectionsResponseDTO> getAll(String organizationId) {
        log.info("Fetching all collections for organizationId: {}", organizationId);
        return collectionsRepository.findByOrganizationId(organizationId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    public void delete(String id) {
        collectionsRepository.deleteById(id);
    }

    private CollectionsResponseDTO convertToResponse(Collections entity) {
        CollectionsResponseDTO dto = new CollectionsResponseDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
