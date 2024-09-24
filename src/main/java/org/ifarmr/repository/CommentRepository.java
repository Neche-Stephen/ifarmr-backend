package org.ifarmr.repository;

import org.ifarmr.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByUserIdOrderByDateCreatedDesc(Long userId);
    @Query("SELECT c FROM Comment c WHERE c.post.user.id = :userId AND c.user.id != :userId ORDER BY c.dateCreated DESC")
    List<Comment> findCommentsOnUserPosts(@Param("userId") Long userId);

    int countByPostId(Long postId);

    List<Comment> findByPostId(Long postId);
}