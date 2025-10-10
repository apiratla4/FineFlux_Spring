package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.InventoryCreateDTO;
import com.pulse.fineflux.domain.InventoryResponseDTO;
import com.pulse.fineflux.domain.InventoryUpdateDTO;
import com.pulse.fineflux.entity.Inventory;
import com.pulse.fineflux.entity.InventoryLog;
import com.pulse.fineflux.entity.Product;
import com.pulse.fineflux.repository.InventoryRepository;
import com.pulse.fineflux.repository.InventoryLogRepository;
import com.pulse.fineflux.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryLogRepository inventoryLogRepository;
    private final ProductRepository productRepository;
    private final ProfitLossService profitLossService;

    /**
     * Create inventory entry and always insert InventoryLog (for history).
     */
    @Override
    @Transactional
    public InventoryResponseDTO createInventory(InventoryCreateDTO dto) {
        try {
            log.info("Creating inventory for productId={} orgId={}", dto.getProductId(), dto.getOrganizationId());
            Product product = productRepository.findByIdAndOrganizationId(dto.getProductId(), dto.getOrganizationId())
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + dto.getProductId()));

            if (!Boolean.TRUE.equals(product.getStatus())) {
                throw new IllegalStateException("Cannot create inventory for INACTIVE product: " + product.getProductName());
            }

            BigDecimal previousLevel = inventoryLogRepository
                    .findTopByProductIdOrderByLastUpdatedDesc(dto.getProductId())
                    .map(InventoryLog::getCurrentLevel)
                    .orElse(BigDecimal.ZERO);

            BigDecimal newCurrentLevel = previousLevel.add(dto.getCurrentLevel());

            if (newCurrentLevel.compareTo(product.getTankCapacity()) > 0) {
                throw new IllegalStateException(String.format(
                        "Adding %.2f exceeds tank capacity %.2f for product '%s'",
                        dto.getCurrentLevel(), product.getTankCapacity(), product.getProductName()
                ));
            }

            BigDecimal totalCapacity = productRepository.findByOrganizationId(dto.getOrganizationId()).stream()
                    .map(p -> p.getTankCapacity() != null ? p.getTankCapacity() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal price = product.getPrice() != null ? BigDecimal.valueOf(product.getPrice()) : BigDecimal.ZERO;
            BigDecimal stockValue = price.multiply(newCurrentLevel);

            Inventory inventory = Inventory.builder()
                    .organizationId(dto.getOrganizationId())
                    .productId(dto.getProductId())
                    .productName(product.getProductName())
                    .totalCapacity(totalCapacity)
                    .stockValue(stockValue)
                    .lastUpdated(new Date())
                    .empId(dto.getEmpId())
                    .currentLevel(newCurrentLevel)
                    .metric(dto.getMetric())
                    .status(true)
                    .tankCapacity(product.getTankCapacity())
                    .build();

            Inventory savedInventory = inventoryRepository.save(inventory);

            // Sync product's currentLevel
            product.setCurrentLevel(newCurrentLevel);
            productRepository.save(product);

            // ALWAYS create InventoryLog record (history row) on create
            InventoryLog logEntry = InventoryLog.builder()
                    .inventoryId(savedInventory.getInventoryId())
                    .organizationId(savedInventory.getOrganizationId())
                    .productId(savedInventory.getProductId())
                    .productName(savedInventory.getProductName())
                    .totalCapacity(savedInventory.getTotalCapacity())
                    .stockValue(savedInventory.getStockValue())
                    .lastUpdated(savedInventory.getLastUpdated())
                    .empId(savedInventory.getEmpId())
                    .currentLevel(savedInventory.getCurrentLevel())
                    .metric(savedInventory.getMetric())
                    .status(savedInventory.getStatus())
                    .tankCapacity(savedInventory.getTankCapacity())
                    .build();
            inventoryLogRepository.save(logEntry);

            profitLossService.calculateAndSaveProfitLoss(dto.getOrganizationId());

            log.debug("Inventory created and log inserted: inventoryId={}, productId={}", savedInventory.getInventoryId(), dto.getProductId());
            return mapToResponse(savedInventory);

        } catch (Exception e) {
            log.error("Error creating inventory productId={} orgId={}", dto.getProductId(), dto.getOrganizationId(), e);
            throw e;
        }
    }

    /**
     * Update inventory and always insert InventoryLog (for history).
     */
    @Override
    @Transactional
    public List<InventoryResponseDTO> updateInventory(String orgId, String productId, InventoryUpdateDTO dto) {
        try {
            log.info("Updating inventory productId={} orgId={}", productId, orgId);

            Inventory prevInventory = inventoryRepository.findByOrganizationIdAndProductId(orgId, productId)
                    .orElseThrow(() -> new RuntimeException("Inventory not found"));

            // Insert a NEW inventory record every update (do NOT update the old one)
            Inventory inventory = Inventory.builder()
                    .organizationId(orgId)
                    .productId(productId)
                    .productName(prevInventory.getProductName())
                    .totalCapacity(dto.getTotalCapacity())
                    .stockValue(dto.getStockValue())
                    .lastUpdated(new Date())
                    .empId(dto.getEmpId())
                    .currentLevel(dto.getCurrentLevel())
                    .metric(dto.getMetric())
                    .status(dto.getStatus())
                    .tankCapacity(dto.getTankCapacity())
                    .build();

            Inventory newRecord = inventoryRepository.save(inventory);

            // Update product currentLevel as before
            productRepository.findByIdAndOrganizationId(productId, orgId)
                    .ifPresent(product -> {
                        product.setCurrentLevel(dto.getCurrentLevel());
                        productRepository.save(product);
                    });

            // ALWAYS create InventoryLog record (history row) on update
            InventoryLog historyLog = InventoryLog.builder()
                    .inventoryId(newRecord.getInventoryId())
                    .organizationId(newRecord.getOrganizationId())
                    .productId(newRecord.getProductId())
                    .productName(newRecord.getProductName())
                    .totalCapacity(newRecord.getTotalCapacity())
                    .stockValue(newRecord.getStockValue())
                    .lastUpdated(newRecord.getLastUpdated())
                    .empId(newRecord.getEmpId())
                    .currentLevel(newRecord.getCurrentLevel())
                    .metric(newRecord.getMetric())
                    .status(newRecord.getStatus())
                    .tankCapacity(newRecord.getTankCapacity())
                    .build();
            inventoryLogRepository.save(historyLog);

            profitLossService.calculateAndSaveProfitLoss(orgId);

            log.debug("Inventory updated BY INSERT (new record), log inserted, and product currentLevel synced, new inventoryId={}", newRecord.getInventoryId());
            return inventoryRepository.findAllByOrganizationId(orgId).stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error updating inventory productId={} orgId={}", productId, orgId, e);
            throw e;
        }
    }

    /**
     * Get all inventories for an organization.
     */
    @Override
    public List<InventoryResponseDTO> getAllInventories(String orgId) {
        try {
            log.info("Fetching all inventories for orgId={}", orgId);
            List<InventoryResponseDTO> inventories = inventoryRepository.findAllByOrganizationId(orgId).stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
            log.debug("Fetched {} inventories for orgId={}", inventories.size(), orgId);
            return inventories;
        } catch (Exception e) {
            log.error("Error fetching inventories for orgId={}", orgId, e);
            throw e;
        }
    }

    /**
     * Delete an inventory entry by id.
     */
    @Override
    @Transactional
    public void deleteInventory(String orgId, String inventoryId) {
        try {
            log.info("Deleting inventory id={} orgId={}", inventoryId, orgId);
            inventoryRepository.deleteById(inventoryId);
            log.debug("Inventory deleted successfully inventoryId={}", inventoryId);
        } catch (Exception e) {
            log.error("Error deleting inventory inventoryId={} orgId={}", inventoryId, orgId, e);
            throw e;
        }
    }

    /**
     * Mapper: Inventory entity to InventoryResponseDTO.
     */
    private InventoryResponseDTO mapToResponse(Inventory entity) {
        return InventoryResponseDTO.builder()
                .inventoryId(entity.getInventoryId())
                .organizationId(entity.getOrganizationId())
                .productId(entity.getProductId())
                .productName(entity.getProductName())
                .totalCapacity(entity.getTotalCapacity())
                .stockValue(entity.getStockValue())
                .lastUpdated(entity.getLastUpdated())
                .empId(entity.getEmpId())
                .currentLevel(entity.getCurrentLevel())
                .metric(entity.getMetric())
                .status(entity.getStatus())
                .tankCapacity(entity.getTankCapacity())
                .build();
    }
}
