package org.ifarmr.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.ifarmr.enums.NotificationType;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "notification_tbl")
public class Notification extends BaseClass {

    private String content;

    @Enumerated(EnumType.STRING)
    private NotificationType type; // Enum for the type of notification (e.g., NEW_POST, NEW_LIKE)

    private boolean isRead = false;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference("notification-user")
    private User user;

}
