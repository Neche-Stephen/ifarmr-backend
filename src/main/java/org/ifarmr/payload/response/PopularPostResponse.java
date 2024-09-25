package org.ifarmr.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ifarmr.entity.Comment;
import org.ifarmr.entity.Like;
import org.ifarmr.entity.User;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PopularPostResponse {
    private Long id;

    private String title;

    private String content;

    private String photoUrl;

    private LocalDateTime dateCreated;

    private Integer commentCount;

    private Integer likeCount;

    private UserSummary postedBy;

    private List<UserSummary> commentedBy;

}
