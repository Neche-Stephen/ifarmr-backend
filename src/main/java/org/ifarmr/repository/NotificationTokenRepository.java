package org.ifarmr.repository;

import org.ifarmr.entity.NotificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationTokenRepository extends JpaRepository<NotificationToken, Long> {

    void deleteByToken(String token);
    NotificationToken findByToken(String token);
    // Find all tokens by user ID
    List<NotificationToken> findByUserId(Long userId);
    void deleteByUserIdAndToken(Long userId, String token);
    boolean existsByUserIdAndToken(Long userId, String token);

}
