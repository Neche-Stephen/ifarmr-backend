package org.ifarmr.service;

import org.ifarmr.payload.request.CropRequest;
import org.ifarmr.payload.response.CropResponse;
import org.ifarmr.payload.response.CropSummaryResponse;

import java.util.List;

public interface CropService {

    CropResponse addCrop(CropRequest cropRequest, String username);

    List<CropResponse> getAllCropsByUser(String username);

    List<CropSummaryResponse> getCropSummaryByUser(String username);

    int totalCrop(String username);
}
 