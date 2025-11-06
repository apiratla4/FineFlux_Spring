package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.*;
import com.pulse.fineflux.entity.Collections;
import com.pulse.fineflux.entity.Sales;
import com.pulse.fineflux.entity.SaleHistory;
import com.pulse.fineflux.repository.CollectionsRepository;
import com.pulse.fineflux.repository.SalesRepository;
import com.pulse.fineflux.repository.SaleHistoryRepository;
import com.pulse.fineflux.utill.SaleMatch;
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
    private final FinanceSummaryService financeSummaryService;

    private static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");

    @Override
    public CollectionsResponseDTO create(CollectionsCreateDTO dto) {
        log.info("Creating collection orgId={}, empId={}, product={}, guns={}, price={}, dateTime={}",
                dto.getOrganizationId(), dto.getEmpId(), dto.getProductName(), dto.getGuns(), dto.getPrice(), dto.getDateTime());

        if (dto.getDateTime() == null) {
            throw new IllegalArgumentException("dateTime is required for collection creation");
        }

        // 1) Normalize to IST second
        LocalDateTime istSecond = SaleMatch.toIstSecondPlus50(dto.getDateTime(), ZoneId.systemDefault()); // withZoneSameInstant → toLocalDateTime → truncatedTo(SECONDS) [web:23][web:29]

        // 2) Normalize keys + keep display
        String productDisplay = dto.getProductName().trim();
        String gunsDisplay = dto.getGuns().trim();
        String productNorm = SaleMatch.normalize(productDisplay); // lower-case, trimmed [web:16]
        String gunsNorm = SaleMatch.normalize(gunsDisplay); // lower-case, trimmed [web:16]

        // 3) Build deterministic match-key at second precision
        String saleMatchKey = SaleMatch.buildKey(istSecond, productNorm, gunsNorm, dto.getPrice()); // "yyyy-MM-dd'T'HH:mm:ss|product|guns|price" [web:2][web:5]

        // 4) Try exact match by org + match key (fast path)
        Sales matchingSale = salesRepository
                .findByOrganizationIdAndSaleMatchKey(dto.getOrganizationId(), saleMatchKey)
                .orElse(null); // manual-ref style lookup [web:2][web:5]

        // 5) Fallback: your original day-window search capped at collection time
        if (matchingSale == null) {
            LocalDateTime dayStart = istSecond.toLocalDate().atStartOfDay();
            LocalDateTime dayEnd = dayStart.plusDays(1);
            final double EPSILON = 0.01;

            List<Sales> salesList = salesRepository.findSalesForCollection(
                    dto.getOrganizationId(), dto.getEmpId(), productNorm, gunsNorm, dayStart, dayEnd
            );

            matchingSale = salesList.stream()
                    .filter(s -> s.getProductName().equals(productNorm)
                            && s.getGuns().equals(gunsNorm)
                            && Math.abs(s.getPrice() - dto.getPrice()) < EPSILON
                            && !s.getDateTime().isAfter(istSecond))
                    .max(Comparator.comparing(Sales::getDateTime))
                    .orElse(null); // preserves your original selection rule [web:14]
        }

        // 6) Money math
        double expectedTotal = (matchingSale != null) ? matchingSale.getSalesInRupees() : 0.0;
        double receivedTotal = dto.getCashReceived() + dto.getPhonePay() + dto.getCreditCard();
        double difference = receivedTotal - expectedTotal;

        // 7) Build collection entity
        Collections entity = new Collections();
        entity.setOrganizationId(dto.getOrganizationId());
        entity.setEmpId(dto.getEmpId());
        entity.setDateTime(istSecond);
        entity.setProductName(productNorm);
        entity.setGuns(gunsNorm);
        entity.setExpectedTotal(expectedTotal);
        entity.setReceivedTotal(receivedTotal);

        if (difference > 0) {
            entity.setAccessCollections(difference);
            entity.setShortCollections(0.0);
        } else {
            entity.setAccessCollections(0.0);
            entity.setShortCollections(expectedTotal - receivedTotal);
        }

        if (matchingSale != null) {
            entity.setSaleId(matchingSale.getSaleId());          // stable UUID from Sales [web:12][web:17]
            entity.setSaleMatchKey(matchingSale.getSaleMatchKey()); // store the deterministic key [web:2][web:5]
        } else {
            entity.setSaleId(null);
            entity.setSaleMatchKey(saleMatchKey); // attempted key for diagnostics [web:2][web:5]
        }

        entity.setCashReceived(dto.getCashReceived());
        entity.setPhonePay(dto.getPhonePay());
        entity.setCreditCard(dto.getCreditCard());

        // 8) Persist and trigger finance summary
        Collections saved = collectionsRepository.save(entity); // standard Spring Data Mongo save [web:14]
        financeSummaryService.autoCreateFinanceSummary(saved.getOrganizationId()); // keep existing trigger [web:14]

        // 9) Write SaleHistory only if a sale was matched
        if (matchingSale != null) {
            SaleHistory history = SaleHistory.builder()
                    .saleId(matchingSale.getSaleId()) // stable link for downstream reporting [web:12][web:17]
                    .organizationId(matchingSale.getOrganizationId())
                    .dateTime(istSecond)
                    .productName(productNorm)
                    .guns(gunsNorm)
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
                    .lastUpdated(LocalDateTime.now(ZoneId.of("Asia/Kolkata")))
                    .build();
            saleHistoryRepository.save(history); // manual reference write [web:2][web:5]
            log.info("SaleHistory inserted with saleId={} orgId={}", matchingSale.getSaleId(), matchingSale.getOrganizationId());
        } else {
            log.warn("No matching Sale found for key={}, product={}, guns={}, price={}; expectedTotal set to 0.0",
                    saleMatchKey, productNorm, gunsNorm, dto.getPrice());
        }

        // 10) Build response with display names
        CollectionsResponseDTO resp = new CollectionsResponseDTO();
        resp.setId(saved.getId());
        resp.setSaleId(saved.getSaleId());
        resp.setOrganizationId(saved.getOrganizationId());
        resp.setEmpId(saved.getEmpId());
        resp.setDateTime(saved.getDateTime());
        resp.setProductName(saved.getProductName());
        resp.setGuns(saved.getGuns());

        resp.setExpectedTotal(saved.getExpectedTotal());
        resp.setReceivedTotal(saved.getReceivedTotal());
        resp.setShortCollections(saved.getShortCollections());
        resp.setAccessCollections(saved.getAccessCollections());
        return resp;
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