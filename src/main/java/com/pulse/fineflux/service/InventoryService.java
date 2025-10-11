package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.*;
import java.util.List;

public interface InventoryService {
    InventoryResponseDTO createInventory(InventoryCreateDTO dto);

    List<InventoryResponseDTO> updateInventory(String orgId, String productId, InventoryUpdateDTO dto);

    List<InventoryResponseDTO> getAllInventories(String orgId);
    // New method to fetch the latest record
    InventoryResponseDTO getLatestInventory(String orgId, String productId);
    void deleteInventory(String orgId, String inventoryId);
}
