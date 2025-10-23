package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.MeterSalesDayDynamicDTO;
import com.pulse.fineflux.entity.Sales;
import com.pulse.fineflux.repository.SalesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeterSalesSummaryServiceImpl implements MeterSalesSummaryService {

    private final SalesRepository salesRepository;

    @Override
    public List<MeterSalesDayDynamicDTO> getMeterSalesSummary(String orgId, String product) {
        log.info("Fetching meter sales summary for orgId={}, product={}", orgId, product);

        // Fetch all sales for org + product (case insensitive)
        List<Sales> allSales = salesRepository
                .findByOrganizationIdAndProductNameIgnoreCaseOrderByDateTime(orgId, product.trim());

        if (allSales.isEmpty()) {
            log.warn("No sales data found for orgId={}, product={}", orgId, product);
            return Collections.emptyList();
        }

        // Normalize product name for strict comparison
        String normalizedProduct = product.trim().toLowerCase();

        // ✅ STRICT FILTER: Only keep sales matching orgId + product exactly
        List<Sales> filteredSales = allSales.stream()
                .filter(s -> s.getOrganizationId().equals(orgId))
                .filter(s -> s.getProductName().trim().equalsIgnoreCase(normalizedProduct))
                .collect(Collectors.toList());

        log.info("After strict filtering: {} sales records for orgId={}, product={}",
                filteredSales.size(), orgId, product);

        if (filteredSales.isEmpty()) {
            log.warn("No sales records after filtering for orgId={}, product={}", orgId, product);
            return Collections.emptyList();
        }

        // Dynamically discover all guns for this orgId + product
        Set<String> allGuns = filteredSales.stream()
                .map(s -> s.getGuns().trim().toLowerCase())
                .collect(Collectors.toSet());

        log.info("Discovered {} guns for orgId={}, product={}: {}",
                allGuns.size(), orgId, product, allGuns);

        // ✅ Group sales by date (yyyy-MM-dd) AFTER filtering
        Map<String, List<Sales>> dateGroup = filteredSales.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getDateTime().toLocalDate().toString(),
                        TreeMap::new,
                        Collectors.toList()
                ));

        double cumulative = 0.0;
        List<MeterSalesDayDynamicDTO> summaryList = new ArrayList<>();

        for (Map.Entry<String, List<Sales>> entry : dateGroup.entrySet()) {
            String date = entry.getKey();
            List<Sales> salesOnDay = entry.getValue();

            log.debug("Date: {}, Sales count: {}", date, salesOnDay.size());

            Map<String, Double> openingReadingsPerGun = new HashMap<>();
            Map<String, Double> salesInLitersPerGun = new HashMap<>();

            // For each gun dynamically found
            for (String gun : allGuns) {
                Optional<Sales> sale = salesOnDay.stream()
                        .filter(s -> gun.equalsIgnoreCase(s.getGuns().trim()))
                        .findFirst();

                openingReadingsPerGun.put(gun, sale.map(Sales::getOpeningStock).orElse(0.0));
                salesInLitersPerGun.put(gun, sale.map(Sales::getSalesInLiters).orElse(0.0));
            }

            // Sum testingTotal for all guns on this date
            double testingTotal = salesOnDay.stream()
                    .filter(s -> allGuns.contains(s.getGuns().trim().toLowerCase()))
                    .mapToDouble(Sales::getTestingTotal)
                    .sum();

            // Sum asPerMeterSale for all guns on this date
            double asPerMeterSale = salesOnDay.stream()
                    .filter(s -> allGuns.contains(s.getGuns().trim().toLowerCase()))
                    .mapToDouble(Sales::getSalesInLiters)
                    .sum();

            log.debug("Date: {}, asPerMeterSale: {}, testingTotal: {}", date, asPerMeterSale, testingTotal);

            cumulative += asPerMeterSale;

            summaryList.add(MeterSalesDayDynamicDTO.builder()
                    .date(date)
                    .openingReadingsPerGun(openingReadingsPerGun)
                    .salesInLitersPerGun(salesInLitersPerGun)
                    .testingTotal(testingTotal)
                    .asPerMeterSale(asPerMeterSale)
                    .cumulativeLiters(cumulative)
                    .build());
        }

        log.info("Returning {} date summaries for orgId={}, product={}",
                summaryList.size(), orgId, product);
        return summaryList;
    }
}
