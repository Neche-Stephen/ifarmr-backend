package org.ifarmr.repository;

import org.ifarmr.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByPostIdAndUserId(Long postId, Long userId);
    List<Like> findByUserIdOrderByDateCreatedDesc(Long userId);
    @Query("SELECT l FROM Like l " +
            "LEFT JOIN FETCH l.post p " +
            "LEFT JOIN FETCH l.comment c " +
            "WHERE (p.user.id = :userId OR c.user.id = :userId) " +
            "AND l.user.id <> :userId " +
            "ORDER BY l.dateCreated DESC")
    List<Like> findLikesOnUserContent(@Param("userId") Long userId);

}