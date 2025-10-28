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
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectionsServiceImpl implements CollectionsService {

    private final CollectionsRepository collectionsRepository;
    private final SalesRepository salesRepository;
    private final SaleHistoryRepository saleHistoryRepository;

    private static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");

    @Override
    public CollectionsResponseDTO create(CollectionsCreateDTO dto) {
        log.info("Creating collection for orgId={}, empId={}, product={}, guns={}, price={}, dateTime={}",
                dto.getOrganizationId(), dto.getEmpId(), dto.getProductName(), dto.getGuns(), dto.getPrice(), dto.getDateTime());

        if (dto.getDateTime() == null) {
            throw new IllegalArgumentException("dateTime is required for collection creation");
        }

        // Convert to IST
        LocalDateTime istDateTime = dto.getDateTime()
                .atZone(ZoneId.systemDefault())
                .withZoneSameInstant(IST_ZONE)
                .toLocalDateTime();

        // Normalize (force lower case for matching and storage)
        String normProduct = dto.getProductName().trim().toLowerCase();
        String normGuns = dto.getGuns().trim().toLowerCase();
        String displayProduct = dto.getProductName().trim();
        String displayGuns = dto.getGuns().trim();

        // Create collections entity and normalize product/guns
        Collections entity = new Collections();
        BeanUtils.copyProperties(dto, entity);
        entity.setDateTime(istDateTime);
        entity.setProductName(normProduct);
        entity.setGuns(normGuns);

        // IST day window logic
        LocalDateTime dayStart = istDateTime.toLocalDate().atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);

        log.info("Sales query for orgId={} empId={} product={} guns={} price={} between {} and {}",
                dto.getOrganizationId(), dto.getEmpId(), normProduct, normGuns, dto.getPrice(), dayStart, dayEnd);

        List<Sales> salesList = salesRepository.findSalesForCollection(
                dto.getOrganizationId(), dto.getEmpId(), normProduct, normGuns, dayStart, dayEnd
        );

        log.info("Sales query returned {} rows", salesList.size());
        for (Sales sale : salesList) {
            log.info("SALE: orgId={}, empId={}, product={}, guns={}, dateTime={}, price={}, salesInRupees={}",
                    sale.getOrganizationId(), sale.getEmpId(), sale.getProductName(), sale.getGuns(),
                    sale.getDateTime(), sale.getPrice(), sale.getSalesInRupees());
        }

        final double EPSILON = 0.01; // Accept 1 paisa as equal! Change as needed.

        Sales matchingSale = salesList.stream()
                .filter(s -> s.getProductName().trim().equalsIgnoreCase(normProduct)
                        && s.getGuns().trim().equalsIgnoreCase(normGuns)
                        && Math.abs(s.getPrice() - dto.getPrice()) < EPSILON
                        && !s.getDateTime().isAfter(istDateTime)) // Key line: only sales up to collection time!
                .max(Comparator.comparing(Sales::getDateTime))
                .orElse(null);


        double expectedTotal = (matchingSale != null) ? matchingSale.getSalesInRupees() : 0.0;
        double receivedTotal = dto.getCashReceived() + dto.getPhonePay() + dto.getCreditCard();

        double difference = receivedTotal - expectedTotal;
        double accessCollections;

        if (difference > 0) {
            accessCollections = difference;
            entity.setAccessCollections(accessCollections); // <--- persist excess!
            entity.setShortCollections(0.0);
        } else {
            accessCollections = 0.0;
            entity.setAccessCollections(0.0);
            entity.setShortCollections(expectedTotal - receivedTotal);
        }

        entity.setExpectedTotal(expectedTotal);
        entity.setReceivedTotal(receivedTotal);
        Collections saved = collectionsRepository.save(entity);

        // Only save SaleHistory if matching sale found
        if (matchingSale != null) {
            SaleHistory history = SaleHistory.builder()
                    .organizationId(matchingSale.getOrganizationId())
                    .dateTime(istDateTime) // use IST for consistency
                    .productName(normProduct)
                    .guns(normGuns)
                    .empId(matchingSale.getEmpId())
                    .openingStock(matchingSale.getOpeningStock())
                    .closingStock(matchingSale.getClosingStock())
                    .testingTotal(matchingSale.getTestingTotal())
                    .salesInLiters(matchingSale.getSalesInLiters())
                    .price(matchingSale.getPrice())
                    .salesInRupees(matchingSale.getSalesInRupees())
                    .cashReceived(saved.getCashReceived())
                    .phonePay(saved.getPhonePay())
                    .creditCard(saved.getCreditCard())
                    .shortCollections(saved.getShortCollections())
                    .receivedTotal(saved.getReceivedTotal())
                    .build();
            saleHistoryRepository.save(history);
            log.info("SaleHistory inserted for saleId={} empId={} orgId={}",
                    matchingSale.getId(), matchingSale.getEmpId(), matchingSale.getOrganizationId());
        } else {
            log.warn("No matching Sale found for: product={}, guns={}, price={}. expectedTotal set to 0.0.",
                    normProduct, normGuns, dto.getPrice());
        }

        CollectionsResponseDTO responseDto = convertToResponse(saved);
        responseDto.setProductName(displayProduct);
        responseDto.setGuns(displayGuns);
        responseDto.setAccessCollections(accessCollections); // ONLY for UI/report, not stored in mongo

        return responseDto;
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
                    .productName(sale.getProductName().trim().toLowerCase())  // <-- NORMALIZE
                    .guns(sale.getGuns().trim().toLowerCase())
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