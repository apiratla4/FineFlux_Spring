package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.FinanceSummaryCreateDTO;
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
import java.util.List;

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

    @Override
    public FinanceSummaryResponseDTO update(String id, FinanceSummaryUpdateDTO dto) {
        FinanceSummary entity = financeRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("FinanceSummary not found"));
        entity.setCashReceived(fmt(dto.getCashReceived()));
        entity.setPhonePay(fmt(dto.getPhonePay()));
        entity.setCreditCard(fmt(dto.getCreditCard()));
        entity.setPetrolInventory(fmt(dto.getPetrolInventory()));
        entity.setDeiselInventory(fmt(dto.getDeiselInventory()));
        entity.setFPetrolInventory(fmt(dto.getFPetrolInventory()));
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
        try {
            if (orgId == null) {
                log.error("autoCreateFinanceSummary called with null orgId");
                throw new IllegalArgumentException("orgId cannot be null");
            }
            log.warn("autoCreateFinanceSummary START orgId={}", orgId);

            LocalDate today = LocalDate.now();
            LocalDateTime dayStart = today.atStartOfDay();
            LocalDateTime dayEnd = dayStart.plusDays(1);

            // 1) Collections totals (today)
            List<Collections> cols = collectionsRepository.findByOrganizationIdAndDateTimeBetween(orgId, dayStart, dayEnd);
            double cashReceived = fmt(cols.stream().mapToDouble(Collections::getCashReceived).sum());
            double phonePay = fmt(cols.stream().mapToDouble(Collections::getPhonePay).sum());
            double creditCard = fmt(cols.stream().mapToDouble(Collections::getCreditCard).sum());

            // 2) Inventory values per product (latest currentLevel * product.price)
            double petrolInventory = fmt(getInventoryValue(orgId, "Petrol"));
            double deiselInventory = fmt(getInventoryValue(orgId, "Deisel"));
            double fPetrolInventory = fmt(getInventoryValue(orgId, "F-Petrol"));
            double cngInventory = fmt(getInventoryValue(orgId, "CNG"));
            double twoTInventory = fmt(getInventoryValue(orgId, "2T"));

            // 3) Today expenses
            List<Expense> expenses = expenseRepo.findByOrganizationId(orgId);
            double totalExpenses = fmt(
                    expenses.stream()
                            .filter(e -> e.getExpenseDate() != null && e.getExpenseDate().equals(today))
                            .mapToDouble(Expense::getAmount)
                            .sum()
            );

            // 4) Total
            double total = fmt(
                    cashReceived + phonePay + creditCard
                            + petrolInventory + deiselInventory + fPetrolInventory + cngInventory + twoTInventory
                            - totalExpenses
            );

            FinanceSummary summary = FinanceSummary.builder()
                    .organizationId(orgId)
                    .createdAt(LocalDateTime.now())
                    .cashReceived(cashReceived)
                    .phonePay(phonePay)
                    .creditCard(creditCard)
                    .petrolInventory(petrolInventory)
                    .deiselInventory(deiselInventory)
                    .fPetrolInventory(fPetrolInventory)
                    .cngInventory(cngInventory)
                    .twoTInventory(twoTInventory)
                    .totalExpenses(totalExpenses)
                    .description("updated")
                    .total(total)
                    .build();

            FinanceSummary saved = financeRepo.save(summary);
            log.warn("autoCreateFinanceSummary END orgId={} savedId={}", orgId, saved.getId());
            return toResponse(saved);

        } catch (Exception e) {
            log.error("Error auto-creating FinanceSummary for orgId={}: {}", orgId, e.getMessage(), e);
            throw e;
        }
    }

    private double getInventoryValue(String orgId, String productName) {
        try {
            Product p = productRepository.findByOrganizationId(orgId).stream()
                    .filter(prod -> productName.equalsIgnoreCase(prod.getProductName()))
                    .findFirst()
                    .orElse(null);
            if (p == null) return 0.0;

            InventoryLog latest = inventoryLogRepository
                    .findTopByProductIdOrderByLastUpdatedDesc(p.getId())
                    .orElse(null);

            if (latest != null && latest.getCurrentLevel() != null && p.getPrice() != null) {
                double value = latest.getCurrentLevel().doubleValue() * p.getPrice().doubleValue();
                return fmt(value);
            }
            return 0.0;
        } catch (Exception e) {
            log.error("Error computing inventory value for orgId={} product={}: {}", orgId, productName, e.getMessage(), e);
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
                .createdAt(e.getCreatedAt())
                .cashReceived(fmt(e.getCashReceived()))
                .phonePay(fmt(e.getPhonePay()))
                .creditCard(fmt(e.getCreditCard()))
                .petrolInventory(fmt(e.getPetrolInventory()))
                .deiselInventory(fmt(e.getDeiselInventory()))
                .fPetrolInventory(fmt(e.getFPetrolInventory()))
                .cngInventory(fmt(e.getCngInventory()))
                .twoTInventory(fmt(e.getTwoTInventory()))
                .totalExpenses(fmt(e.getTotalExpenses()))
                .description(e.getDescription())
                .total(fmt(e.getTotal()))
                .build();
    }
}
