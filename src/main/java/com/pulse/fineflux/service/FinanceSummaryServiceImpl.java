package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.FinanceSummaryResponseDTO;
import com.pulse.fineflux.domain.FinanceSummaryUpdateDTO;
import com.pulse.fineflux.entity.*;
import com.pulse.fineflux.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinanceSummaryServiceImpl implements FinanceSummaryService {

    private final FinanceSummaryRepository financeRepo;
    private final CollectionsRepository collectionsRepository;
    private final InventoryRepository inventoryRepo;
    private final InventoryLogRepository inventoryLogRepository;
    private final ProductRepository productRepository;
    private final ExpenseRepository expenseRepo;
    private final SalesRepository salesRepo;

    @Override
    public FinanceSummaryResponseDTO update(String id, FinanceSummaryUpdateDTO dto) {
        FinanceSummary entity = financeRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("FinanceSummary not found"));
        entity.setCashReceived(fmt(dto.getCashReceived()));
        entity.setPhonePay(fmt(dto.getPhonePay()));
        entity.setCreditCard(fmt(dto.getCreditCard()));
        entity.setPetrolInventory(fmt(dto.getPetrolInventory()));
        entity.setDieselInventory(fmt(dto.getDieselInventory()));
        entity.setPremiumPetrolInventory(fmt(dto.getPremiumPetrolInventory()));
        entity.setCngInventory(fmt(dto.getCngInventory()));
        entity.setTwoTInventory(fmt(dto.getTwoTInventory()));
        entity.setTotalExpenses(fmt(dto.getTotalExpenses()));
        entity.setDescription(dto.getDescription());
        entity.setTotal(fmt(dto.getTotal()));
        FinanceSummary saved = financeRepo.save(entity);
        log.info("FinanceSummary updated: {}", saved.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FinanceSummaryResponseDTO autoCreateFinanceSummary(String orgId) {
        if (orgId == null) throw new IllegalArgumentException("orgId cannot be null");

        ZoneId IST = ZoneId.of("Asia/Kolkata");
        LocalDate today = LocalDate.now(IST);

        LocalDateTime istStart = today.atStartOfDay();
        LocalDateTime istEnd = istStart.plusDays(1);

        // Convert to UTC for querying
        LocalDateTime utcStart = istStart.atZone(IST).withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();
        LocalDateTime utcEnd = istEnd.atZone(IST).withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();

        Optional<FinanceSummary> existingOpt = financeRepo
                .findTopByOrganizationIdAndCreatedAtBetweenOrderByCreatedAtDesc(orgId, utcStart, utcEnd);

        FinanceSummary summary = existingOpt.orElseGet(FinanceSummary::new);

        boolean isNew = summary.getId() == null;

        // Fetch all sales for the org and filter by IST local date to ensure deletes/rollbacks are reflected
        double salesRevenue = fmt(salesRepo.findByOrganizationId(orgId).stream()
                .filter(s -> s.getDateTime() != null && s.getDateTime().toLocalDate().equals(today))
                .mapToDouble(Sales::getSalesInRupees).sum());

        double petrolInventory = fmt(getInventoryValue(orgId, "Petrol"));
        double dieselInventory = fmt(getInventoryValue(orgId, "Diesel"));
        double premiumPetrolInventory = fmt(getInventoryValue(orgId, "Premium Petrol"));
        double cngInventory = fmt(getInventoryValue(orgId, "CNG"));
        double twoTInventory = fmt(getInventoryValue(orgId, "2T"));

        // Fetch all collections for the org and filter by IST local date
        List<Collections> collections = collectionsRepository.findByOrganizationId(orgId).stream()
                .filter(c -> c.getDateTime() != null && c.getDateTime().toLocalDate().equals(today))
                .collect(Collectors.toList());

        double cashReceived = fmt(collections.stream().mapToDouble(Collections::getCashReceived).sum());
        double phonePay     = fmt(collections.stream().mapToDouble(Collections::getPhonePay).sum());
        double creditCard   = fmt(collections.stream().mapToDouble(Collections::getCreditCard).sum());

        // 3) Today expenses
        List<Expense> expenses = expenseRepo.findByOrganizationId(orgId);
        double totalExpenses = fmt( expenses.stream() .filter(e -> e.getExpenseDate() != null && e.getExpenseDate().equals(today)) .mapToDouble(Expense::getAmount) .sum() );

        double totalRevenue = fmt(salesRevenue + petrolInventory + dieselInventory +
                premiumPetrolInventory + cngInventory + twoTInventory);

        double netTotal = fmt(totalRevenue - totalExpenses);

        // Update fields
        summary.setOrganizationId(orgId);
        if (isNew) summary.setCreatedAt(LocalDateTime.now()); // only set if new
        summary.setCashReceived(cashReceived);
        summary.setPhonePay(phonePay);
        summary.setCreditCard(creditCard);
        summary.setPetrolInventory(petrolInventory);
        summary.setDieselInventory(dieselInventory);
        summary.setPremiumPetrolInventory(premiumPetrolInventory);
        summary.setCngInventory(cngInventory);
        summary.setTwoTInventory(twoTInventory);
        summary.setTotalExpenses(totalExpenses);
        summary.setTotal(netTotal);
        summary.setDescription("updated");

        FinanceSummary saved = financeRepo.save(summary);
        return toResponse(saved);
    }

    private double getInventoryValue(String orgId, String productName) {
        try {
            Inventory latest = inventoryRepo.findAllByOrganizationId(orgId).stream()
                    .filter(inv -> inv.getProductName() != null && inv.getProductName().equalsIgnoreCase(productName))
                    .max((a, b) -> b.getLastUpdated().compareTo(a.getLastUpdated()))
                    .orElse(null);
            if (latest != null && latest.getStockValue() != null) {
                double value = latest.getStockValue().doubleValue();
                log.debug("Latest Inventory for orgId={} productName={} is stockValue={}", orgId, productName, value);
                return fmt(value);
            }
            log.warn("No inventory found or missing stockValue for orgId={} productName={}", orgId, productName);
            return 0.0;
        } catch (Exception e) {
            log.error("Error computing inventory value for orgId={} productName={}: {}", orgId, productName, e.getMessage(), e);
            return 0.0;
        }
    }

    private double fmt(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    @Override
    public FinanceSummaryResponseDTO getLatestByOrg(String orgId) {
        FinanceSummary e = financeRepo.findTopByOrganizationIdOrderByCreatedAtDesc(orgId);
        if (e == null) throw new RuntimeException("No FinanceSummary found for orgId=" + orgId);
        return toResponse(e);
    }

    @Override
    public List<FinanceSummaryResponseDTO> getAllByOrg(String orgId) {
        return financeRepo.findByOrganizationId(orgId).stream().map(this::toResponse).toList();
    }

    private FinanceSummaryResponseDTO toResponse(FinanceSummary e) {
        return FinanceSummaryResponseDTO.builder()
                .id(e.getId())
                .organizationId(e.getOrganizationId())
                .createdAt(e.getCreatedAt().atZone(ZoneId.of("UTC"))
                        .withZoneSameInstant(ZoneId.of("Asia/Kolkata"))
                        .toLocalDateTime())
                .cashReceived(fmt(e.getCashReceived()))
                .phonePay(fmt(e.getPhonePay()))
                .creditCard(fmt(e.getCreditCard()))
                .petrolInventory(fmt(e.getPetrolInventory()))
                .dieselInventory(fmt(e.getDieselInventory()))
                .premiumPetrolInventory(fmt(e.getPremiumPetrolInventory()))
                .cngInventory(fmt(e.getCngInventory()))
                .twoTInventory(fmt(e.getTwoTInventory()))
                .totalExpenses(fmt(e.getTotalExpenses()))
                .description(e.getDescription())
                .total(fmt(e.getTotal()))
                .build();
    }
}
