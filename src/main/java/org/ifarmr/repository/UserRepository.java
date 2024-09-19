package org.ifarmr.repository;

import org.ifarmr.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name != 'ADMIN'")
    Page<User> findAll(Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u WHERE DATE(u.dateJoined) = :specificDate")
    List<Long> findTotalUsersForDate(@Param("specificDate") LocalDate specificDate);

    @Query("SELECT COUNT(u) FROM User u WHERE DATE(u.lastActive) = :specificDate")
    List<Long> findActiveUsersForDate(@Param("specificDate") LocalDate specificDate);
}
