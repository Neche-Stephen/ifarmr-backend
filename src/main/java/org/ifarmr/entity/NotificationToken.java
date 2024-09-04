package org.ifarmr.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "notification_token_tbl")
public class NotificationToken extends BaseClass {
    @Column(nullable = false)
    private Long userId;  // User ID associated with the token

    @Column(nullable = false, unique = true)
    private String token;  // Token should be unique and not null
}
