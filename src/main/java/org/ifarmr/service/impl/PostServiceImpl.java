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
import org.ifarmr.payload.response.CommentResponseDto;
import org.ifarmr.payload.response.PopularPostResponse;
import org.ifarmr.payload.response.PostResponse;
import org.ifarmr.payload.response.UserSummary;
import org.ifarmr.repository.CommentRepository;
import org.ifarmr.repository.LikeRepository;
import org.ifarmr.repository.PostRepository;
import org.ifarmr.repository.UserRepository;
import org.ifarmr.service.NotificationService;
import org.ifarmr.service.PostService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
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


        if(user == null){
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
            return "Post unliked successfully.";
        } else {
            Like like = new Like();
            like.setPost(post);
            like.setUser(user);
            likeRepository.save(like);

            // Get the owner of the post
            User postOwner = post.getUser();

            // Send notification to the owner of the post
            NotificationRequest notificationRequest = new NotificationRequest();
            notificationRequest.setTitle(generateLikeNotificationTitle(like));
            notificationRequest.setBody(generateLikeNotificationDescription(like));
            notificationRequest.setTopic("Like Notifications");

            try {
                notificationService.sendNotificationToUser(postOwner.getUsername(), notificationRequest);
            } catch (ExecutionException | InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Failed to send notification to the post owner", e);
            }
            return "Post liked successfully.";
        }
    }

    @Override
    public CommentResponseDto commentOnPost(String username, CommentDto commentDto) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Post post = postRepository.findById(commentDto.getPostId())
                .orElseThrow(() -> new NotFoundException("Post not found"));

        Comment comment = new Comment();
        comment.setContent(commentDto.getContent());
        comment.setParentContentId(commentDto.getParentContentId());
        comment.setDateCreated(LocalDateTime.now());
        comment.setPost(post);
        comment.setUser(user);

        Comment savedComment = commentRepository.save(comment);

        // Get the owner of the post or comment
        User postOwner = post.getUser();

         //Send notification to the owner of the post
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setTitle(generateCommentNotificationTitle(savedComment));
        notificationRequest.setBody(generateCommentNotificationDescription(savedComment));
        notificationRequest.setTopic("Comment Notifications");

        try {
            notificationService.sendNotificationToUser(postOwner.getUsername(), notificationRequest);
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to send notification to the post owner", e);
        }

        // Return the saved comment details as CommentDto
        CommentResponseDto savedCommentDto = new CommentResponseDto();
        savedCommentDto.setCommentId(savedComment.getId());
        savedCommentDto.setFullName(user.getFullName());
        savedCommentDto.setContent(savedComment.getContent());
        savedCommentDto.setParentContentId(savedComment.getParentContentId());
        savedCommentDto.setPostId(post.getId());

        return savedCommentDto;
    }

    @Override
    public List<PostResponse> getPostsByUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        List<Post> userPosts = postRepository.findByUserId(user.getId());

        return userPosts.stream()
                .map(post -> PostResponse.builder()
                        .id(post.getId())
                        .title(post.getTitle())
                        .content(post.getContent())
                        .photoUrl(post.getPhotoUrl())
                        .dateCreated(post.getDateCreated())
                        .likeCount(likeRepository.countByPostId(post.getId())) // Set like count
                        .commentCount(commentRepository.countByPostId(post.getId())) // Set comment count
                        .userName(user.getUsername()) // Include userName in the response
                        .build())
                .collect(Collectors.toList());
    }
    @Override
    public PostResponse getPostDetails(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .photoUrl(post.getPhotoUrl())
                .dateCreated(post.getDateCreated())
                .fullName(post.getUser().getFullName())

                .likeCount(likeRepository.countByPostId(postId)) // Count of likes
                .commentCount(commentRepository.countByPostId(postId)) // Count of comments
                .build();
    }


    @Override
    public List<String> getLikesForPost(Long postId) {
        List<Like> likes = likeRepository.findByPostId(postId);
        return likes.stream()
                .map(like -> like.getUser().getUsername())
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentResponseDto> getCommentsForPost(Long postId) {
        List<Comment> comments = commentRepository.findByPostId(postId);

        return comments.stream()
                .map(comment -> CommentResponseDto.builder()
                        .commentId(comment.getId())
                        .postId(postId)
                        .content(comment.getContent())
                        .fullName(comment.getUser().getFullName())
                        .dateCreated(comment.getDateCreated())
                        .parentContentId(comment.getParentContentId())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public CommentResponseDto replyToComment(String username, CommentDto commentDto) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Post post = postRepository.findById(commentDto.getPostId())
                .orElseThrow(() -> new NotFoundException("Post not found"));

        Comment parentComment = commentRepository.findById(commentDto.getParentContentId())
                .orElseThrow(() -> new NotFoundException("Parent comment not found"));

        Comment reply = new Comment();
        reply.setContent(commentDto.getContent());
        reply.setParentContentId(commentDto.getParentContentId());
        reply.setDateCreated(LocalDateTime.now());
        reply.setPost(post);
        reply.setUser(user);

        Comment savedReply = commentRepository.save(reply);

        CommentResponseDto savedReplyDto = new CommentResponseDto();
        savedReplyDto.setCommentId(savedReply.getId());
        savedReplyDto.setContent(savedReply.getContent());
        savedReplyDto.setParentContentId(savedReply.getParentContentId());
        savedReplyDto.setPostId(post.getId());
        savedReplyDto.setFullName(user.getFullName());
        savedReplyDto.setDateCreated(savedReply.getDateCreated());

        return savedReplyDto;
    }

    @Override
    public List<CommentResponseDto> getRepliesForComment(Long commentId) {
        List<Comment> replies = commentRepository.findByParentContentId(commentId);
        return replies.stream()
                .map(reply -> CommentResponseDto.builder()
                        .commentId(reply.getId())
                        .content(reply.getContent())
                        .postId(reply.getPost().getId())
                        .parentContentId(reply.getParentContentId())
                        .fullName(reply.getUser().getFullName())
                        .dateCreated(reply.getDateCreated())
                        .build())
                .collect(Collectors.toList() );
    }

    private String generateLikeNotificationTitle(Like like) {
        if (like.getPost() != null) {
            return "Your Post Received a Like";
        } else if (like.getComment() != null) {
            return "Your Comment Received a Like";
        }
        return "Content Received a Like";
    }
    private String generateLikeNotificationDescription(Like like) {
        if (like.getPost() != null) {
            return like.getUser().getUsername() + " liked your post: \"" + like.getPost().getTitle() + "\"";
        } else if (like.getComment() != null) {
            return like.getUser().getUsername() + " liked your comment: \"" + like.getComment().getContent() + "\"";
        }
        return "A user liked your content";
    }
    private String generateCommentNotificationTitle(Comment comment) {
        return "New Comment on Your Post";
    }
    private String generateCommentNotificationDescription(Comment comment) {
        return comment.getUser().getUsername() + " commented: \"" + comment.getContent() + "\"";
    }


    public List<PopularPostResponse> getPopularPosts() {
        List<Post> posts = postRepository.findAll();
        // Sort posts based on the popularity score and return the top 3
        return posts.stream()
                .sorted(Comparator.comparingInt(this::popularityScore).reversed())
                .limit(4)
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
                .id(post.getId())
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

            Map<Long, Comment> uniqueUsersMap = comments.stream()
                    .collect(Collectors.toMap(
                            comment -> comment.getUser().getId(), // Key: User ID
                            comment -> comment, // Value: Comment object
                            (existing, replacement) -> existing, // In case of duplicates, keep the first one
                            LinkedHashMap::new // Maintain insertion order
                    ));
            return uniqueUsersMap.values().stream()
                    .sorted(Comparator.comparing(Comment::getDateCreated).reversed())
                    .limit(3)
                    .map(comment -> mapToUserSummary(comment.getUser()))
                    .collect(Collectors.toList());
        }

}
