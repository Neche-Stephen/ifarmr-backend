package org.ifarmr.service.impl;

import org.ifarmr.entity.LiveStock;
import org.ifarmr.entity.User;
import org.ifarmr.exceptions.BadRequestException;
import org.ifarmr.exceptions.ConflictException;
import org.ifarmr.exceptions.IFarmServiceException;
import org.ifarmr.exceptions.NotFoundException;
import org.ifarmr.payload.request.LiveStockRequest;
import org.ifarmr.payload.response.LiveStockInfo;
import org.ifarmr.payload.response.LiveStockResponse;
import org.ifarmr.payload.response.LivestockSummaryInfo;
import org.ifarmr.payload.response.LivestockSummaryResponse;
import org.ifarmr.repository.LiveStockRepository;
import org.ifarmr.repository.UserRepository;
import org.ifarmr.service.GlobalUploadService;
import org.ifarmr.service.LiveStockService;
import org.ifarmr.utils.FileUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LiveStockServiceImpl implements LiveStockService {

    @Autowired
    private LiveStockRepository liveStockRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GlobalUploadService globalUploadService;

    @Override
    public LiveStockResponse addLiveStock(LiveStockRequest liveStockRequest, String username) {
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new NotFoundException("User not found with username: " + username));

            // Validate animal name
            if (liveStockRequest.getAnimalName() == null || liveStockRequest.getAnimalName().isEmpty()) {
                throw new BadRequestException("Animal name cannot be null or empty");
            }

            // Handle photo upload
            String uploadedPhotoUrl = null;
            if (liveStockRequest.getPhotoUpload() != null && !liveStockRequest.getPhotoUpload().isEmpty()) {
                FileUploadUtil.assertAllowed(liveStockRequest.getPhotoUpload(), FileUploadUtil.IMAGE_PATTERN);
                uploadedPhotoUrl = globalUploadService.uploadImage(liveStockRequest.getPhotoUpload());
            }

            // Create a LiveStock entity from the LiveStockRequest
            LiveStock liveStock = LiveStock.builder()
                    .animalName(liveStockRequest.getAnimalName())
                    .animalType(liveStockRequest.getAnimalType())
                    .breed(liveStockRequest.getBreed())
                    .quantity(liveStockRequest.getQuantity())
                    .age(liveStockRequest.getAge())
                    .wateringFrequency(liveStockRequest.getWateringFrequency())
                    .feedingSchedule(liveStockRequest.getFeedingSchedule())
                    .vaccinationSchedule(liveStockRequest.getVaccinationSchedule())
                    .healthIssues(liveStockRequest.getHealthIssues())
                    .description(liveStockRequest.getDescription())
                    .status(liveStockRequest.getStatus())
                    .photoUpload(uploadedPhotoUrl)
                    .location(liveStockRequest.getLocation())
                    .user(user) // Set the current user to the livestock
                    .build();

            // Save the livestock entity to the database
            LiveStock savedLiveStock = liveStockRepository.save(liveStock);

            // Build and return the response
            return LiveStockResponse.builder()
                    .responseMessage("Livestock Added Successfully!")
                    .liveStockInfo(LiveStockInfo.builder()
                            .animalName(savedLiveStock.getAnimalName())
                            .animalType(savedLiveStock.getAnimalType())
                            .breed(savedLiveStock.getBreed())
                            .quantity(savedLiveStock.getQuantity())
                            .age(savedLiveStock.getAge())
                            .wateringFrequency("Every " + savedLiveStock.getWateringFrequency() + " days")
                            .feedingSchedule("Every " + savedLiveStock.getFeedingSchedule() + " days")
                            .vaccinationSchedule("Every " + savedLiveStock.getVaccinationSchedule() + " days")
                            .healthIssues(savedLiveStock.getHealthIssues())
                            .description(savedLiveStock.getDescription())
                            .status(savedLiveStock.getStatus())
                            .photoUpload(savedLiveStock.getPhotoUpload())
                            .location(savedLiveStock.getLocation())
                            .build())
                    .build();

        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Data integrity issue: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Validation failed: " + e.getMessage());
        } catch (Exception e) {
            throw new IFarmServiceException("An error occurred while adding the livestock: " + e.getMessage(), e);
        }
    }

    @Override
    public List<LiveStockResponse> getAllLiveStockByUser(String username) {
        List<LiveStock> liveStockList = liveStockRepository.findByUser_Username(username);

        return liveStockList.stream()
                .map(liveStock -> LiveStockResponse.builder()
                        .responseMessage("Livestock Retrieved Successfully!")
                        .liveStockInfo(LiveStockInfo.builder()
                                .animalName(liveStock.getAnimalName())
                                .animalType(liveStock.getAnimalType())
                                .breed(liveStock.getBreed())
                                .quantity(liveStock.getQuantity())
                                .age(liveStock.getAge())
                                .wateringFrequency("Every " + liveStock.getWateringFrequency() + " days")
                                .feedingSchedule("Every " + liveStock.getFeedingSchedule() + " days")
                                .vaccinationSchedule("Every " + liveStock.getVaccinationSchedule() + " days")
                                .healthIssues(liveStock.getHealthIssues())
                                .description(liveStock.getDescription())
                                .status(liveStock.getStatus())
                                .photoUpload(liveStock.getPhotoUpload())
                                .location(liveStock.getLocation())
                                .build())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<LivestockSummaryResponse> getLivestockSummaryByUser(String username) {
        List<LiveStock> liveStockList = liveStockRepository.findByUser_Username(username);
        return liveStockList.stream()
                .map(liveStock -> LivestockSummaryResponse.builder()
                        .responseMessage("Livestock Retrieved Successfully!")
                        .livestockSummaryInfo(LivestockSummaryInfo.builder()
                                .animalName(liveStock.getAnimalName())
                                .quantity(liveStock.getQuantity())
                                .status(liveStock.getStatus())
                                .location(liveStock.getLocation())
                                .createdDate(liveStock.getDateCreated())
                                .build())
                        .build())
                .collect(Collectors.toList());

    }

    @Override
    public int totalLiveStock(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User with " + username +" not found"));

        return liveStockRepository.countByUser(user);
    }
}
