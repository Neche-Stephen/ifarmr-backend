package org.ifarmr.controller;


import lombok.RequiredArgsConstructor;
import org.ifarmr.payload.request.CommentDto;
import org.ifarmr.payload.request.PostRequest;
import org.ifarmr.payload.response.CommentResponseDto;
import org.ifarmr.payload.response.PopularPostResponse;
import org.ifarmr.payload.response.PostResponse;
import org.ifarmr.service.PostService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponse> createPost(@ModelAttribute PostRequest postRequest) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Extract username from the Authentication object
        String userName = authentication.getName();

        // Call the service method to create the post
        PostResponse postResponse = postService.createPost(postRequest, userName);

        return ResponseEntity.ok(postResponse);
    }

    @PostMapping("/{postId}/like")
    public String likeOrUnlikePost(@PathVariable Long postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        return postService.likeOrUnlikePost(postId, currentUsername);
    }

    @PostMapping("/comment")
    public CommentResponseDto commentOnPost(@RequestBody CommentDto commentDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        return postService.commentOnPost(currentUsername, commentDto);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<PopularPostResponse>> getPopularPosts() {
        List<PopularPostResponse> topPosts = postService.getPopularPosts();
        return ResponseEntity.ok(topPosts);
    }

    @GetMapping("/user-posts")
    public ResponseEntity<List<PostResponse>> getPostsByUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        List<PostResponse> userPosts = postService.getPostsByUser(currentUsername);
        return ResponseEntity.ok(userPosts);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPostDetails(@PathVariable Long postId){

        PostResponse postResponse = postService.getPostDetails(postId);
        return ResponseEntity.ok(postResponse);
    }

    @GetMapping("/{postId}/likes")
    public ResponseEntity<List<String>> getLikesForPost(@PathVariable Long postId){
        List<String> likes = postService.getLikesForPost(postId);
        return ResponseEntity.ok(likes);
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<CommentResponseDto>> getCommentsForPost(@PathVariable Long postId){
        List<CommentResponseDto> comments = postService.getCommentsForPost(postId);
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/comments/reply")
    public CommentResponseDto replyToComment(@RequestBody CommentDto commentDto){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        return postService.replyToComment(currentUsername, commentDto);
    }

    @GetMapping("/comments/{commentId}/replies")
    public ResponseEntity<List<CommentResponseDto>> getRepliesForComment(@PathVariable Long commentId){
        List<CommentResponseDto> replies = postService.getRepliesForComment(commentId);
        return ResponseEntity.ok(replies);
    }
}
