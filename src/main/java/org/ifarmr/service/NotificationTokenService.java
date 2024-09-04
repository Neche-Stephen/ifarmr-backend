package org.ifarmr.service;

import org.ifarmr.entity.NotificationToken;

import java.util.List;

public interface NotificationTokenService {

    List<NotificationToken> getTokensByUserId(Long userId);

    NotificationToken saveToken(String username, String token);  // Updated to accept username

    void deleteToken(String username, String token);  // Updated to accept username

    boolean tokenExists(String token);
}
