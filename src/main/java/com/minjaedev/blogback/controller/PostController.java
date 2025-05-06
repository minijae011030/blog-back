package com.minjaedev.blogback.controller;

import com.minjaedev.blogback.common.ApiResponse;
import com.minjaedev.blogback.dto.post.PostRequestDto;

import com.minjaedev.blogback.repository.PostRepository;
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
    private final PostRepository postRepository;

    // 게시글 단건 조회 메서드
    @GetMapping("/{postSeq}")
    public ResponseEntity<ApiResponse<?>> getPostBySeq(@PathVariable Long postSeq, HttpServletRequest request) {
        return postService.getPostBySeq(postSeq, request);
    }

    // 게시글 다건 조회 메서드
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

    // 고정 게시글 다건 조회 메서드
    @GetMapping("/pinned")
    public ResponseEntity<ApiResponse<?>> getPinnedPosts(
            @RequestHeader String blogId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return postService.getPinnedPosts(blogId, page, size);
    }

    // 포스팅 메서드
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createPost(
            @RequestBody PostRequestDto requestDto,
            HttpServletRequest request
    ) {
        return postService.createPost(requestDto, request);
    }

    // 게시글 수정 메서드
    @PutMapping("/{postSeq}")
    public ResponseEntity<ApiResponse<?>> updatePost(
            @PathVariable Long postSeq,
            @RequestBody PostRequestDto requestDto,
            HttpServletRequest request
    ) {
        return postService.updatePost(postSeq, requestDto, request);
    }

    // 게시글 삭제 메서드
    @DeleteMapping("/{postSeq}")
    public ResponseEntity<ApiResponse<?>> deletePost(
            @PathVariable Long postSeq,
            HttpServletRequest request
    ) {
        return postService.deletePost(postSeq, request);
    }

    // 게시글 고정 메서드
    @PostMapping("/{postSeq}/pin")
    public ResponseEntity<ApiResponse<?>> pinPost(
            @PathVariable Long postSeq,
            HttpServletRequest request
    ) {
        return postService.setPinned(postSeq, request, true);
    }

    // 게시글 고정 해제 메서드
    @PostMapping("/{postSeq}/unpin")
    public ResponseEntity<ApiResponse<?>> unpinPost(
            @PathVariable Long postSeq,
            HttpServletRequest request
    ) {
        return postService.setPinned(postSeq, request, false);
    }

    // 보관 게시글 메서드
    @GetMapping("/archived")
    public ResponseEntity<ApiResponse<?>> getArchivedPosts(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return postService.getArchivedPosts(request, page, size);
    }

    @PostMapping("/{postSeq}/archive")
    public ResponseEntity<ApiResponse<?>> archivePost(
            @PathVariable Long postSeq,
            HttpServletRequest request
    ) {
        return postService.setArchived(postSeq, request, true);
    }

    @PostMapping("/{postSeq}/unarchive")
    public ResponseEntity<ApiResponse<?>> unarchivePost(
            @PathVariable Long postSeq,
            HttpServletRequest request
    ) {
        return postService.setArchived(postSeq, request, false);
    }

    // 인기글 조회 메서드
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<?>> getPopularPosts() {
        return postService.getPopularPosts();
    }

    // 검색 메서드
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<?>> searchPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return postService.searchPosts(keyword, page, size);
    }

    @GetMapping("/calendar")
    public ResponseEntity<ApiResponse<?>> getPostDaysByMonth(
            @RequestHeader String blogId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        return postService.getPostDaysByMonth(blogId, year, month);
    }
}
