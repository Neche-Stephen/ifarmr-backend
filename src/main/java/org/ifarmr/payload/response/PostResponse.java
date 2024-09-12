package org.ifarmr.payload.response;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostResponse {

    private String message;

    private String title;

    private String content;

    private String photoUrl;

    private LocalDateTime dateCreated;

    private String userName;

    private int likeCount;   // For number of likes

    private int commentCount; // For number of comments
}
