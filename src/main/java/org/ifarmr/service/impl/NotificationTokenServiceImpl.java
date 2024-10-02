package org.ifarmr.service.impl;

import org.ifarmr.entity.NotificationToken;
import org.ifarmr.entity.User;
import org.ifarmr.exceptions.NotFoundException;
import org.ifarmr.repository.NotificationTokenRepository;
import org.ifarmr.repository.UserRepository;
import org.ifarmr.service.NotificationTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationTokenServiceImpl implements NotificationTokenService {

    @Autowired
    private NotificationTokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<NotificationToken> getTokensByUserId(Long userId) {
        return tokenRepository.findByUserId(userId);
    }

    @Override
    public boolean tokenExists(String username, String token) {
        // Find the user by username to get userId
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Check by userId and token
        return tokenRepository.existsByUserIdAndToken(user.getId(), token);
    }


    @Override
    @Transactional
    public NotificationToken saveToken(String userName, String token) {
        User user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new NotFoundException("User not found"));

        NotificationToken notificationToken = new NotificationToken();
        notificationToken.setUserId(user.getId());
        notificationToken.setToken(token);
        return tokenRepository.save(notificationToken);
    }

    @Override
    @Transactional
    public void deleteToken(String userName, String token) {
        User user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new NotFoundException("User not found"));

        tokenRepository.deleteByUserIdAndToken(user.getId(), token);
    }
}
