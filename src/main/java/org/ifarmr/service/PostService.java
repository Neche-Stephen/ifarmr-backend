package org.ifarmr.service;

import org.ifarmr.entity.Post;
import org.ifarmr.payload.request.CommentDto;
import org.ifarmr.payload.request.PostRequest;
import org.ifarmr.payload.response.CommentResponseDto;
import org.ifarmr.payload.response.PopularPostResponse;
import org.ifarmr.payload.response.PostResponse;

import java.util.List;

public interface PostService {

    PostResponse createPost(PostRequest postRequest, String userName);

    String likeOrUnlikePost(Long postId, String username);

    CommentResponseDto commentOnPost(String username, CommentDto commentDto);

     List<PopularPostResponse> getPopularPosts();

    List<PostResponse> getPostsByUser(String username);

    PostResponse getPostDetails(Long postId);


    List<String> getLikesForPost(Long postId);

    List<CommentResponseDto> getCommentsForPost(Long postId);

    CommentResponseDto replyToComment(String username, CommentDto commentDto);

    List <CommentResponseDto> getRepliesForComment(Long commentId);
}
