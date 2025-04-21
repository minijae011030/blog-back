package com.minjaedev.blogback.controller;

import com.minjaedev.blogback.common.ApiResponse;
import com.minjaedev.blogback.dto.post.PostRequestDto;


import com.minjaedev.blogback.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @GetMapping("/{postSeq}")
    public ResponseEntity<ApiResponse<?>> getPostBySeq(@PathVariable Long postSeq) {
        return postService.getPostBySeq(postSeq);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getPosts(
            @RequestHeader String blogId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tag
    ) {
        return postService.getPosts(blogId, page, size, category, tag);
    }

    @GetMapping("/pinned")
    public ResponseEntity<ApiResponse<?>> getPinnedPosts(
            @RequestHeader String blogId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return postService.getPinnedPosts(blogId, page, size);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createPost(
            @RequestBody PostRequestDto requestDto,
            HttpServletRequest request
    ) {
        return postService.createPost(requestDto, request);
    }

    @PutMapping("/{postSeq}")
    public ResponseEntity<ApiResponse<?>> updatePost(
            @PathVariable Long postSeq,
            @RequestBody PostRequestDto requestDto,
            HttpServletRequest request
    ) {
        return postService.updatePost(postSeq, requestDto, request);
    }

    @DeleteMapping("/{postSeq}")
    public ResponseEntity<ApiResponse<?>> deletePost(
            @PathVariable Long postSeq,
            HttpServletRequest request
    ) {
        return postService.deletePost(postSeq, request);
    }

    @PostMapping("/{postSeq}/pin")
    public ResponseEntity<ApiResponse<?>> pinPost(
            @PathVariable Long postSeq,
            HttpServletRequest request
    ) {
        return postService.setPinned(postSeq, request, true);
    }

    @PostMapping("/{postSeq}/unpin")
    public ResponseEntity<ApiResponse<?>> unpinPost(
            @PathVariable Long postSeq,
            HttpServletRequest request
    ) {
        return postService.setPinned(postSeq, request, false);
    }
}
