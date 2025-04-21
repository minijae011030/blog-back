package com.minjaedev.blogback.controller;

import com.minjaedev.blogback.common.ApiResponse;
import com.minjaedev.blogback.domain.Post;
import com.minjaedev.blogback.domain.User;
import com.minjaedev.blogback.dto.post.PostCreateResponseDto;
import com.minjaedev.blogback.dto.post.PostRequestDto;
import com.minjaedev.blogback.dto.post.PostResponseDto;
import com.minjaedev.blogback.jwt.JwtProvider;
import com.minjaedev.blogback.repository.PostRepository;
import com.minjaedev.blogback.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController {
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @GetMapping("/{postSeq}")
    public ResponseEntity<ApiResponse<?>> getPostBySeq(@PathVariable Long postSeq) {
        Post post = postRepository.findById(postSeq.toString()).orElse(null);

        if (post == null) {
            return ResponseEntity.status(404).body(ApiResponse.of(404, "해당 게시글을 찾을 수 없습니다."));
        }

        return ResponseEntity.ok(ApiResponse.of(200, "게시글 조회 성공", new PostResponseDto(post)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createPost(@RequestBody PostRequestDto requestDto, HttpServletRequest request) {
        String token = jwtProvider.resolveToken(request.getHeader("Authorization"));
        if (token == null || !jwtProvider.validateToken(token)) {
            return ResponseEntity.status(401).body(ApiResponse.of(401, "유효하지 않은 토큰입니다."));
        }

        String userId = jwtProvider.getUserIdFromToken(token);
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(ApiResponse.of(404, "사용자를 찾을 수 없습니다."));
        }

        Post newPost = Post.builder()
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .category(requestDto.getCategory())
                .author(user)
                .build();

        Post savedPost = postRepository.save(newPost);

        savedPost.setTags(requestDto.getTags());
        postRepository.save(savedPost);

        return ResponseEntity.ok(ApiResponse.of(200, "게시글 작성 완료", new PostCreateResponseDto(savedPost.getPostSeq())));
    }
}
