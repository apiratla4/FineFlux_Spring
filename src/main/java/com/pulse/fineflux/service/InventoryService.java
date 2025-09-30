package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.*;
import java.util.List;

public interface InventoryService {
    InventoryResponseDTO createInventory(InventoryCreateDTO dto);

    List<InventoryResponseDTO> updateInventory(String orgId, String productId, InventoryUpdateDTO dto);

    List<InventoryResponseDTO> getAllInventories(String orgId);

    void deleteInventory(String orgId, String inventoryId);
}
