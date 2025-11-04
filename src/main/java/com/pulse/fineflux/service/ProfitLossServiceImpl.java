package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.ProfitLossResponseDTO;
import com.pulse.fineflux.entity.InventoryLog;
import com.pulse.fineflux.entity.ProfitLoss;
import com.pulse.fineflux.entity.Product;
import com.pulse.fineflux.entity.Collections;
import com.pulse.fineflux.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfitLossServiceImpl implements ProfitLossService {

    private final CollectionsRepository collectionsRepository;
    private final InventoryLogRepository inventoryLogRepository;
    private final ProductRepository productRepository;
    private final ProfitLossRepository profitLossRepository;
   // private final ExpenseService expenseService;

    /**
     * Calculate Profit/Loss and save into MongoDB
     */
    @Override
    @Transactional
    public ProfitLossResponseDTO calculateAndSaveProfitLoss(String orgId) {
        try {
            // 1️⃣ Cash received (sum of all collections for org)
            double cashReceived = collectionsRepository.findByOrganizationId(orgId)
                    .stream()
                    .mapToDouble(Collections::getReceivedTotal)
                    .sum();

            log.debug("Total Cash Received: {}", cashReceived);

            // 2️⃣ Total Expenses
           /* double totalExpenses = expenseService.calculateTotalExpenses(orgId)
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();

            log.debug("Total Expenses: {}", totalExpenses);
*/
            // 3️⃣ Inventory Value
            double inventoryValue = productRepository.findByOrganizationId(orgId)
                    .stream()
                    .mapToDouble(p -> {
                        InventoryLog latest = inventoryLogRepository
                                .findTopByProductIdOrderByLastUpdatedDesc(p.getId())
                                .orElse(null);

                        double value = 0.0;
                        if (latest != null && latest.getCurrentLevel() != null && p.getPrice() != null) {
                            value = latest.getCurrentLevel().doubleValue() * p.getPrice().doubleValue();
                        }
                        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
                    })
                    .sum();

            inventoryValue = BigDecimal.valueOf(inventoryValue)
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();

            log.debug("Inventory Value: {}", inventoryValue);

           double totalExpenses=3000;
            // 4️⃣ Profit/Loss
            double profitLoss = BigDecimal.valueOf(cashReceived + inventoryValue- totalExpenses ) //
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();

            // 5️⃣ Save in MongoDB
            ProfitLoss pl = ProfitLoss.builder()
                    .cashReceived(cashReceived)
                    .inventoryValue(inventoryValue)
                    .totalExpenses(totalExpenses)
                    .profitLoss(profitLoss)
                    .calculatedAt(LocalDateTime.now())
                    .build();

            ProfitLoss saved = profitLossRepository.save(pl);
            log.info("Profit/Loss saved: {}", saved);

            return toDto(saved);

        } catch (Exception e) {
            log.error("Error calculating Profit/Loss for orgId={}", orgId, e);
            throw new RuntimeException("Failed to calculate Profit/Loss", e);
        }
    }

    /**
     * Fetch all historical Profit/Loss records
     */
    @Override
    public List<ProfitLossResponseDTO> getAllProfitLoss() {
        return profitLossRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private ProfitLossResponseDTO toDto(ProfitLoss pl) {
        return ProfitLossResponseDTO.builder()
                .id(pl.getId())
                .cashReceived(pl.getCashReceived())
                .inventoryValue(pl.getInventoryValue())
                .totalExpenses(pl.getTotalExpenses())
                .profitLoss(pl.getProfitLoss())
                .calculatedAt(pl.getCalculatedAt())
                .build();
    }
}
