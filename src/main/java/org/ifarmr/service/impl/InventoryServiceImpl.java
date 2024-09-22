package org.ifarmr.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.ifarmr.entity.Inventory;
import org.ifarmr.entity.User;
import org.ifarmr.exceptions.AlreadyExistsException;
import org.ifarmr.exceptions.FileUploadException;
import org.ifarmr.exceptions.FunctionErrorException;
import org.ifarmr.exceptions.NotFoundException;
import org.ifarmr.payload.request.InventoryRequest;
import org.ifarmr.payload.request.NotificationRequest;
import org.ifarmr.payload.response.InventoryResponse;
import org.ifarmr.payload.response.InventoriesResponse;
import org.ifarmr.repository.InventoryRepository;
import org.ifarmr.repository.UserRepository;
import org.ifarmr.service.InventoryService;
import org.ifarmr.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;
    private final Cloudinary cloudinary;
    private final NotificationService notificationService;


    @Override
    public InventoryResponse createInventory(InventoryRequest inventoryRequest, String username) {
      User user =   userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User with " + username +" not found"));

        if (inventoryRepository.existsByNameAndItemType(inventoryRequest.getName(), inventoryRequest.getItemType())) {
            throw new AlreadyExistsException("Inventory with the name and item already exists");
        }

        String fileUrl = null;
        MultipartFile file = inventoryRequest.getFile();
        if (file != null && !file.isEmpty()) {
            try {
                //FileUploadUtil.assertAllowed(file, FileUploadUtil.IMAGE_PATTERN);
                Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
                fileUrl = uploadResult.get("url").toString();
            } catch (Exception e) {
                throw new FileUploadException("Failed to Upload your file " + e.getMessage());
            }
        }


       Inventory newInventory = Inventory.builder()
               .user(user)
               .itemType(inventoryRequest.getItemType())
               .name(inventoryRequest.getName())
               .quantity(inventoryRequest.getQuantity())
               .cost(inventoryRequest.getCost())
               .location(inventoryRequest.getLocation())
               .dateAcquired(inventoryRequest.getDateAcquired())
               .currentState(inventoryRequest.getCurrentState())
               .photoUpload(fileUrl)
               .build();
        try{
            inventoryRepository.save(newInventory);

            // SEND NOTIFICATION TO USER
            NotificationRequest notificationRequest = new NotificationRequest();
            notificationRequest.setTitle("New Inventory Added");
            notificationRequest.setBody("A new inventory has been created with name: " + inventoryRequest.getName());
            notificationRequest.setTopic("Inventory Notifications");
            notificationService.sendNotificationToUser(username, notificationRequest);

        }catch (Exception e){
            throw new FunctionErrorException("Unable to save your inventory");
        }


        return InventoryResponse.builder()
                .itemType(newInventory.getItemType().name())
                .name(newInventory.getName())
                .quantity(newInventory.getQuantity())
                .cost(newInventory.getCost())
                .location(newInventory.getLocation())
                .dateAcquired(newInventory.getDateAcquired())
                .currentState(newInventory.getCurrentState())
                .photoUrl(newInventory.getPhotoUpload())
                .build();
    }

    @Override
    public int totalInventory(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User with " + username +" not found"));

            return inventoryRepository.countByUser(user);

    }

    @Override
    public InventoriesResponse getAllInventories() {
        List<Inventory> inventories = inventoryRepository.findAll();
        Long totalInventory = inventoryRepository.findTotalInventory();
        Long totalInventoryValue = inventoryRepository.findTotalInventoryValue();

        List<InventoryResponse> inventoryResponses = inventories.stream()
                .map(inventory -> InventoryResponse.builder()
                        .id(inventory.getId())
                        .name(inventory.getName())
                        .itemType(inventory.getItemType().name())
                        .quantity(inventory.getQuantity())
                        .cost(inventory.getCost())
                        .photoUrl(inventory.getPhotoUpload())
                        .location(inventory.getLocation())
                        .dateAcquired(inventory.getDateAcquired())
                        .currentState(inventory.getCurrentState())
                        .build())
                .collect(Collectors.toList());

        return InventoriesResponse.builder()
                .totalInventory(totalInventory)
                .totalInventoryValue(totalInventoryValue)
                .inventories(inventoryResponses)
                .build();
    }
}
