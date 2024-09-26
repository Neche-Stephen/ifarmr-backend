package org.ifarmr.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Getter
@Setter
@Table(name = "like_tbl", uniqueConstraints = {@jakarta.persistence.UniqueConstraint(columnNames = {"post_id", "user_id"})})
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Like extends BaseClass {

    private Integer countLike;

    @ManyToOne
    @JoinColumn(name = "comment_id")
    @JsonBackReference("like-comment")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Comment comment;

    @ManyToOne
    @JoinColumn(name = "post_id")
    @JsonBackReference("like-post")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Post post;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference("like-user")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private User user;
}
