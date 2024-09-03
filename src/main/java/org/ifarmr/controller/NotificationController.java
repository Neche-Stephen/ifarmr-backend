package org.ifarmr.controller;

import lombok.RequiredArgsConstructor;
import org.ifarmr.payload.request.NotificationRequest;
import org.ifarmr.payload.request.RecentActivityDto;
import org.ifarmr.payload.response.NotificationResponse;
import org.ifarmr.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/recent-activities")
    public ResponseEntity<List<RecentActivityDto>> getRecentActivities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        List<RecentActivityDto> activities = notificationService.getRecentActivities(username);
        return ResponseEntity.ok(activities);
    }

    @PostMapping("/send-to-user/")
    public ResponseEntity<NotificationResponse> sendNotificationToUser(
            @RequestBody NotificationRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        try {
            notificationService.sendNotificationToUser(username, request);
            NotificationResponse response = NotificationResponse.builder()
                    .status(HttpStatus.OK.value())
                    .message("Notification sent to user.")
                    .build();
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            NotificationResponse response = NotificationResponse.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to send notification.")
                    .build();
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/send-to-all")
    public ResponseEntity<NotificationResponse> sendNotificationToAll(@RequestBody NotificationRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        try {
            notificationService.sendNotificationToAll(username, request);
            NotificationResponse response = NotificationResponse.builder()
                    .status(HttpStatus.OK.value())
                    .message("Notification sent to all tokens.")
                    .build();
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            NotificationResponse response = NotificationResponse.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to send notification.")
                    .build();
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
