package org.ifarmr.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.ifarmr.entity.Comment;
import org.ifarmr.entity.Like;
import org.ifarmr.entity.Post;
import org.ifarmr.entity.User;
import org.ifarmr.exceptions.FileUploadException;
import org.ifarmr.exceptions.NotFoundException;
import org.ifarmr.payload.request.CommentDto;
import org.ifarmr.payload.request.NotificationRequest;
import org.ifarmr.payload.request.PostRequest;
import org.ifarmr.payload.response.PopularPostResponse;
import org.ifarmr.payload.response.PostResponse;
import org.ifarmr.payload.response.UserSummary;
import org.ifarmr.repository.CommentRepository;
import org.ifarmr.repository.LikeRepository;
import org.ifarmr.repository.PostRepository;
import org.ifarmr.repository.UserRepository;
import org.ifarmr.service.NotificationService;
import org.ifarmr.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PostServiceImpl implements PostService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final Cloudinary cloudinary;

    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;

    private final NotificationService notificationService;

    @Override
    public PostResponse createPost(PostRequest postRequest, String userName) {

        // get the user who created the post
        User user = userRepository.findByUsername(userName).orElse(null);

        if (user == null) {
            throw new NotFoundException("user not found");
        }

        String fileUrl = null;
        MultipartFile file = postRequest.getFile();
        if (file != null && !file.isEmpty()) {
            try {
                Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
                fileUrl = uploadResult.get("url").toString();
            } catch (Exception e) {
                throw new FileUploadException("Error uploading file to Cloudinary");
            }
        }

        Post post = postRepository.save(Post.builder()
                .title(postRequest.getTitle())
                .content(postRequest.getContent())
                .photoUrl(fileUrl)
                .user(user)
                .build()
        );

        // SEND NOTIFICATION TO ALL USERS
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setTitle("New Post Added");
        notificationRequest.setBody("A new post has been added with title: " + post.getTitle());
        notificationRequest.setTopic("Post Notifications");

        try {
            notificationService.sendNotificationToAll(userName,notificationRequest);
        } catch (ExecutionException | InterruptedException e) {
            // Handle exceptions
            Thread.currentThread().interrupt(); // Restore interrupted state
            throw new RuntimeException("Failed to send notification to all users", e);
        }

        return PostResponse.builder()
                .message("Post created successfully")
                .title(postRequest.getTitle())
                .content(postRequest.getContent())
                .photoUrl(fileUrl)
                .dateCreated(post.getDateCreated())
                .build();
    }

    @Override
    public String likeOrUnlikePost(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Optional<Like> existingLike = likeRepository.findByPostIdAndUserId(postId, user.getId());

        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());

            // SEND NOTIFICATION TO USER
            NotificationRequest notificationRequest = new NotificationRequest();
            notificationRequest.setTitle("New Like");
            notificationRequest.setBody("A post/comment has been liked with title: " + post.getTitle());
            notificationRequest.setTopic("Like Notifications");

            try {
                notificationService.sendNotificationToUser(username, notificationRequest);
            } catch (ExecutionException | InterruptedException e) {
                // Handle exceptions
                Thread.currentThread().interrupt(); // Restore interrupted state
                throw new RuntimeException("Failed to send notification to the user", e);
            }

            return "Post unliked successfully.";
        } else {
            Like like = new Like();
            like.setPost(post);
            like.setUser(user);
            likeRepository.save(like);
            return "Post liked successfully.";
        }
    }

    @Override
    public CommentDto commentOnPost(String username, CommentDto commentDto) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Post post = postRepository.findById(commentDto.getPostId())
                .orElseThrow(() -> new NotFoundException("Post not found"));

        Comment comment = new Comment();
        comment.setContent(commentDto.getContent());
        comment.setParentContentId(commentDto.getParentContentId());
        comment.setPost(post);
        comment.setUser(user);

        Comment savedComment = commentRepository.save(comment);

        // Return the saved comment details as CommentDto
        CommentDto savedCommentDto = new CommentDto();
        savedCommentDto.setContent(savedComment.getContent());
        savedCommentDto.setParentContentId(savedComment.getParentContentId());
        savedCommentDto.setPostId(post.getId());  // Ensure that postId is part of CommentDto

        // SEND NOTIFICATION TO USER
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setTitle("New Comment Added");
        notificationRequest.setBody("A new comment has been added with title: " + post.getTitle());
        notificationRequest.setTopic("Comment Notifications");

        try {
            notificationService.sendNotificationToUser(username, notificationRequest);
        } catch (ExecutionException | InterruptedException e) {
            // Handle exceptions
            Thread.currentThread().interrupt(); // Restore interrupted state
            throw new RuntimeException("Failed to send notification to the user", e);
        }

        return savedCommentDto;
    }

    @Override
    public List<PostResponse> getPostsByUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        List<Post> userPosts = postRepository.findByUserId(user.getId());

        return userPosts.stream()
                .map(post -> PostResponse.builder()
                        .title(post.getTitle())
                        .content(post.getContent())
                        .photoUrl(post.getPhotoUrl())
                        .dateCreated(post.getDateCreated())
                        .build())
                .collect(Collectors.toList());
    }

    public List<PopularPostResponse> getPopularPosts() {
        List<Post> posts = postRepository.findAll();
        // Sort posts based on the popularity score and return the top 3
        return posts.stream()
                .sorted(Comparator.comparingInt(this::popularityScore).reversed())
                .limit(3)
                .map(this::mapToPopularPostResponse)
                .collect(Collectors.toList());
    }
    private int popularityScore(Post post) {
        int commentWeight = 3;
        int likeWeight = 1;

        int totalComments = (post.getComments() != null) ? post.getComments().size() : 0;
        int totalLikes = (post.getLikes() != null) ? post.getLikes().size() : 0;

        return (totalComments * commentWeight) + (totalLikes * likeWeight);
    }
    private PopularPostResponse mapToPopularPostResponse(Post post) {
        return PopularPostResponse.builder()
                .title(post.getTitle())
                .content(post.getContent())
                .photoUrl(post.getPhotoUrl())
                .dateCreated(post.getDateCreated())
                .commentCount((post.getComments() != null) ? post.getComments().size() : 0)
                .likeCount((post.getLikes() != null) ? post.getLikes().size() : 0)
                .postedBy(mapToUserSummary(post.getUser()))
                .commentedBy(mapCommentsToUserSummary(post.getComments()))
                .build();
    }

        private UserSummary mapToUserSummary(User user) {
            if (user == null) return null;
            return UserSummary.builder()
                    .name(user.getFullName())
                    .photoUrl(user.getDisplayPhoto())
                    .build();
        }

        private List<UserSummary> mapCommentsToUserSummary(List<Comment> comments) {
            if (comments == null) return List.of();
            return comments.stream()
                    .sorted(Comparator.comparing(Comment::getDateCreated).reversed())
                    .limit(3)
                    .map(comment -> mapToUserSummary(comment.getUser()))
                    .collect(Collectors.toList());
        }

}
