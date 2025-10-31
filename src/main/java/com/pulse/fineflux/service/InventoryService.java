package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.*;
import com.pulse.fineflux.entity.Inventory;
import com.pulse.fineflux.entity.InventoryLog;

import java.util.List;

public interface InventoryService {
    InventoryResponseDTO createInventory(InventoryCreateDTO dto);

    List<InventoryResponseDTO> updateInventory(String orgId, String productId, InventoryUpdateDTO dto);

    List<InventoryResponseDTO> getAllInventories(String orgId);

    InventoryResponseDTO getLatestInventory(String orgId, String productId);

    void deleteInventory(String orgId, String inventoryId, String employeeId);

    List<Inventory> getInventoriesByProductAndOrg(String orgId, String productId);
    void saveInventory(Inventory inventory);
    InventoryLog getInventoryLogByInventoryId(String inventoryId);
}
