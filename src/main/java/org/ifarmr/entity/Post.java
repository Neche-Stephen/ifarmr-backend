package org.ifarmr.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "post_tbl")
@Builder
public class Post extends BaseClass{

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String photoUrl;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference("post-user")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private User user;

    @OneToMany( mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Comment> comments;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Like> likes;

}
