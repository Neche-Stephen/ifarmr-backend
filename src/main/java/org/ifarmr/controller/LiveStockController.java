package org.ifarmr.controller;

import org.ifarmr.payload.request.LiveStockRequest;
import org.ifarmr.payload.response.LiveStockResponse;
import org.ifarmr.payload.response.LivestockSummaryResponse;
import org.ifarmr.service.LiveStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/livestock")
public class LiveStockController {

    @Autowired
    private LiveStockService liveStockService;

    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LiveStockResponse> addLiveStock(@ModelAttribute LiveStockRequest liveStockRequest) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        LiveStockResponse addedLiveStock = liveStockService.addLiveStock(liveStockRequest, username);

        return ResponseEntity.status(201).body(addedLiveStock);
    }

    @GetMapping("/user")
    public ResponseEntity<List<LiveStockResponse>> getAllLiveStockForUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        List<LiveStockResponse> liveStockResponses = liveStockService.getAllLiveStockByUser(username);
        return ResponseEntity.ok(liveStockResponses);

    }

    @GetMapping("/user-summary")
    public ResponseEntity<List<LivestockSummaryResponse>> getAllLiveStockSummaryByUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        List<LivestockSummaryResponse> livestockSummaryResponses = liveStockService.getLivestockSummaryByUser(username);
        return ResponseEntity.ok(livestockSummaryResponses);

    }

    @GetMapping("/total")
    public ResponseEntity<Integer> getTotalCrops() {
        String username = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        return ResponseEntity.ok(liveStockService.totalLiveStock(username));
    }

}
