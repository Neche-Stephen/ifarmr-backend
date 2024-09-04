package org.ifarmr.repository;

import org.ifarmr.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByUserId(Long userId);
    List<Post> findByUserIdOrderByDateCreatedDesc(Long userId);
}
