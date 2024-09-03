package org.ifarmr.service.impl;

import org.ifarmr.entity.NotificationToken;
import org.ifarmr.entity.User;
import org.ifarmr.exceptions.NotFoundException;
import org.ifarmr.repository.NotificationTokenRepository;
import org.ifarmr.repository.UserRepository;
import org.ifarmr.service.NotificationTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationTokenServiceImpl implements NotificationTokenService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationTokenServiceImpl.class);

    @Autowired
    private NotificationTokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<NotificationToken> getTokensByUserId(Long userId) {
        return tokenRepository.findByUserId(userId);
    }

    public boolean tokenExists(String token) {
        return tokenRepository.existsByToken(token);
    }

    @Override
    @Transactional
    public NotificationToken saveToken(String userName, String token) {
        logger.info("Retrieving user by username: {}", userName);
        User user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new NotFoundException("User not found"));

        logger.info("Saving token for user ID: {}", user.getId());
        NotificationToken notificationToken = new NotificationToken();
        notificationToken.setUserId(user.getId());
        notificationToken.setToken(token);
        return tokenRepository.save(notificationToken);
    }

    @Override
    @Transactional
    public void deleteToken(String userName, String token) {
        logger.info("Retrieving user by username: {}", userName);
        User user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new NotFoundException("User not found"));

        logger.info("Deleting token for user ID: {}", user.getId());
        tokenRepository.deleteByUserIdAndToken(user.getId(), token);
    }
}
