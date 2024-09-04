package org.ifarmr.service.impl;

import com.google.firebase.messaging.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.ifarmr.payload.request.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ExecutionException;

@Service
public class FCMService {
    private Logger logger = LoggerFactory.getLogger(FCMService.class);


    public void sendMessageToToken(NotificationRequest request)
            throws InterruptedException, ExecutionException {
        Message message = getPreconfiguredMessageToToken(request);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonOutput = gson.toJson(message);
        String response = sendAndGetResponse(message);
        logger.info("Sent message to token. Device token: " + request.getToken() + ", " + response+ " msg "+jsonOutput);
    }

//    private String sendAndGetResponse(Message message) throws InterruptedException, ExecutionException {
//        return FirebaseMessaging.getInstance().sendAsync(message).get();
//    }

    private String sendAndGetResponse(Message message) throws InterruptedException, ExecutionException {
        try {
            return FirebaseMessaging.getInstance().sendAsync(message).get();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof FirebaseMessagingException) {
                FirebaseMessagingException fme = (FirebaseMessagingException) e.getCause();
                if ("UNREGISTERED".equals(fme.getMessagingErrorCode().name())) {
                    // Handle the unregistered token, e.g., remove from database
                    logger.error("Token is unregistered. Removing token from database.");
                }
                else{
                    logger.error("Oh boy");
                }
            }
            throw e;
        }
    }



    private AndroidConfig getAndroidConfig(String topic) {
        return AndroidConfig.builder()
                .setTtl(Duration.ofMinutes(2).toMillis()).setCollapseKey(topic)
                .setPriority(AndroidConfig.Priority.HIGH)
                .setNotification(AndroidNotification.builder()
                        .setTag(topic).build()).build();
    }
    private ApnsConfig getApnsConfig(String topic) {
        return ApnsConfig.builder()
                .setAps(Aps.builder().setCategory(topic).setThreadId(topic).build()).build();
    }
    private WebpushConfig getWebpushConfig(String title, String body) {
        return WebpushConfig.builder()
                .putHeader("ttl", "300")
                .setNotification(
                        WebpushNotification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .setIcon("icon-url")
                                .build())
                .build();
    }

    private Message getPreconfiguredMessageToToken(NotificationRequest request) {
        return getPreconfiguredMessageBuilder(request).setToken(request.getToken())
                .build();
    }


    private Message.Builder getPreconfiguredMessageBuilder(NotificationRequest request) {
        AndroidConfig androidConfig = getAndroidConfig(request.getTopic());
        ApnsConfig apnsConfig = getApnsConfig(request.getTopic());
        WebpushConfig webpushConfig = getWebpushConfig(request.getTitle(), request.getBody()); // Pass title and body here
        Notification notification = Notification.builder()
                .setTitle(request.getTitle())
                .setBody(request.getBody())
                .build();
        return Message.builder()
                .setApnsConfig(apnsConfig)
                .setAndroidConfig(androidConfig)
                .setWebpushConfig(webpushConfig) // Add this line
                .setNotification(notification);
    }

}
