package org.ifarmr.service;

import org.ifarmr.payload.request.LiveStockRequest;
import org.ifarmr.payload.response.LiveStockResponse;
import org.ifarmr.payload.response.LivestockSummaryResponse;

import java.util.List;

public interface LiveStockService {

    LiveStockResponse addLiveStock(LiveStockRequest liveStockRequest, String username);

    List<LiveStockResponse> getAllLiveStockByUser(String username);

    List<LivestockSummaryResponse> getLivestockSummaryByUser(String username);

    int totalLiveStock(String username);
}
