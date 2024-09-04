package org.ifarmr.repository;

import org.ifarmr.entity.Task;
import org.ifarmr.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    boolean existsByTitleAndDueDateAndUser(String title, LocalDate dueDate, User user);

    @Query("SELECT t FROM Task t WHERE t.user = :user ORDER BY t.dueDate ASC")
    List<Task> findTasksByUserOrderByDueDateAsc(@Param("user") User user);

    List<Task> findByUserIdOrderByDueDateDesc(Long userId);
}
