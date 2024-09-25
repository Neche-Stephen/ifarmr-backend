package org.ifarmr.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InventoriesResponse {
    private Long totalInventory;
    private Long totalInventoryValue;
    private List<InventoryResponse> inventories;  // List of inventory DTOs
}
