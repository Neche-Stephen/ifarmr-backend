package org.ifarmr.service;

import org.ifarmr.payload.request.NotificationRequest;
import org.ifarmr.payload.request.RecentActivityDto;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface NotificationService {

    List<RecentActivityDto> getRecentActivities(String username);

    void sendNotificationToUser(String username, NotificationRequest request) throws ExecutionException, InterruptedException;

    void sendNotificationToAll(String username, NotificationRequest request) throws ExecutionException, InterruptedException;

}
