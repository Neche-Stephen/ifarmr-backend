package org.ifarmr.service;

import org.ifarmr.payload.request.InventoryRequest;
import org.ifarmr.payload.response.InventoryResponse;
import org.ifarmr.payload.response.InventoriesResponse;

public interface InventoryService {

    InventoryResponse createInventory (InventoryRequest inventoryRequest, String username);

    int totalInventory(String username);

    InventoriesResponse getAllInventories();
}
