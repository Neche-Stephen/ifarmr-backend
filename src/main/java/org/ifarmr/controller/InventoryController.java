package org.ifarmr.controller;

import lombok.RequiredArgsConstructor;
import org.ifarmr.payload.request.InventoryRequest;
import org.ifarmr.payload.response.InventoryResponse;
import org.ifarmr.service.InventoryService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping(value ="/create" , consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<InventoryResponse> createInventory (@ModelAttribute InventoryRequest inventoryRequest) {
        String username = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        return ResponseEntity.ok(inventoryService.createInventory(inventoryRequest, username));
    }

    @GetMapping("/total")
    public ResponseEntity<Integer> getTotalInventory() {
        String username = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        return ResponseEntity.ok(inventoryService.totalInventory(username));
    }

}
