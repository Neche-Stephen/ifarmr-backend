package org.ifarmr.payload.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponseDto {
    private Long commentId;
    private Long postId;
    private String content;
    private Long parentContentId;
    private String fullName;
    private LocalDateTime dateCreated;
}
