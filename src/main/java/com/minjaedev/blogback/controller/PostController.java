package com.minjaedev.blogback.controller;

import com.minjaedev.blogback.common.ApiResponse;
import com.minjaedev.blogback.domain.Category;
import com.minjaedev.blogback.domain.Post;
import com.minjaedev.blogback.domain.Tag;
import com.minjaedev.blogback.domain.User;
import com.minjaedev.blogback.dto.post.PostCreateResponseDto;
import com.minjaedev.blogback.dto.post.PostListResponseDto;
import com.minjaedev.blogback.dto.post.PostRequestDto;
import com.minjaedev.blogback.dto.post.PostResponseDto;
import com.minjaedev.blogback.jwt.JwtProvider;
import com.minjaedev.blogback.repository.CategoryRepository;
import com.minjaedev.blogback.repository.PostRepository;
import com.minjaedev.blogback.repository.TagRepository;
import com.minjaedev.blogback.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController {
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    @GetMapping("/{postSeq}")
    public ResponseEntity<ApiResponse<?>> getPostBySeq(@PathVariable Long postSeq) {
        Post post = postRepository.findById(postSeq).orElse(null);

        if (post == null) {
            return ResponseEntity.status(404).body(
                    ApiResponse.of(404, "해당 게시글을 찾을 수 없습니다."));
        }

        return ResponseEntity.ok(
                ApiResponse.of(200, "게시글 조회 성공", new PostResponseDto(post)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getPosts(
            @RequestHeader String blogId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tag) {
        User user = userRepository.findByBlogId(blogId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(
                    ApiResponse.of(404, "존재하지 않는 회원입니다."));
        }

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> postPage = postRepository.findAllByAuthor(user, pageable);

        if (category != null) {
            Category categoryEntity = categoryRepository.findByNameAndUser(category, user).orElse(null);
            if (categoryEntity == null) {
                return ResponseEntity.status(404).body(
                        ApiResponse.of(404, "해당 카테고리를 찾을 수 없습니다."));
            }
            postPage = postRepository.findAllByAuthorAndCategory(user, categoryEntity, pageable);
        } else if (tag != null) {
            postPage = postRepository.findAllByAuthorAndTags_Name(user, tag, pageable);
        } else {
            postPage = postRepository.findAllByAuthor(user, pageable);
        }


        List<PostResponseDto> postDtos = postPage.getContent().stream()
                .map(PostResponseDto::new)
                .toList();

        PostListResponseDto response = new PostListResponseDto((int) postPage.getTotalElements(), postDtos);
        return ResponseEntity.ok(
                ApiResponse.of(200, "게시글 목록 조회 성공", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createPost(
            @RequestBody PostRequestDto requestDto,
            HttpServletRequest request) {

        String token = jwtProvider.resolveToken(request.getHeader("Authorization"));
        if (token == null || !jwtProvider.validateToken(token)) {
            return ResponseEntity.status(401).body(
                    ApiResponse.of(401, "유효하지 않은 토큰입니다."));
        }

        String userId = jwtProvider.getUserIdFromToken(token);
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(
                    ApiResponse.of(404, "사용자를 찾을 수 없습니다."));
        }

        String categoryName = requestDto.getCategory();
        Category category = categoryRepository.findByNameAndUser(categoryName, user)
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .name(categoryName)
                        .user(user)
                        .build()));

        List<Tag> tagList = new ArrayList<>();
        for (String tagName : requestDto.getTags()) {
            Tag tag = tagRepository.findByNameAndUser(tagName, user)
                    .orElseGet(() -> tagRepository.save(Tag.builder()
                            .name(tagName)
                            .user(user)
                            .build()));
            tagList.add(tag);
        }

        Post newPost = Post.builder()
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .author(user)
                .category(category)
                .tags(tagList)
                .build();

        Post savedPost = postRepository.save(newPost);

        return ResponseEntity.ok(
                ApiResponse.of(200, "게시글 작성 완료", new PostCreateResponseDto(savedPost.getPostSeq())));
    }

    @PutMapping("/{postSeq}")
    public ResponseEntity<ApiResponse<?>> updatePost(
            @PathVariable Long postSeq,
            @RequestBody PostRequestDto requestDto,
            HttpServletRequest request
    ) {
        String token = jwtProvider.resolveToken(request.getHeader("Authorization"));
        if (token == null || !jwtProvider.validateToken(token)) {
            return ResponseEntity.status(401).body(
                    ApiResponse.of(401, "유효하지 않은 토큰입니다."));
        }

        String userId = jwtProvider.getUserIdFromToken(token);
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(
                    ApiResponse.of(404, "사용자를 찾을 수 없습니다."));
        }

        Post post = postRepository.findById(postSeq).orElse(null);
        if (post == null || !post.getAuthor().getId().equals(userId)) {
            return ResponseEntity.status(403).body(ApiResponse.of(403, "수정 권한이 없습니다."));

        }

        // 카테고리 처리
        String categoryName = requestDto.getCategory();
        Category category = categoryRepository.findByNameAndUser(categoryName, user)
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .name(categoryName)
                        .user(user)
                        .build()));

        // 태그 처리
        List<Tag> tagList = new ArrayList<>();
        for (String tagName : requestDto.getTags()) {
            Tag tag = tagRepository.findByNameAndUser(tagName, user)
                    .orElseGet(() -> tagRepository.save(Tag.builder()
                            .name(tagName)
                            .user(user)
                            .build()));
            tagList.add(tag);
        }

        // 포스트 수정
        post.setTitle(requestDto.getTitle());
        post.setContent(requestDto.getContent());
        post.setCategory(category);
        post.setTags(tagList);

        postRepository.save(post);

        return ResponseEntity.ok(
                ApiResponse.of(200, "게시글 수정 완료"));
    }

    @DeleteMapping("/{postSeq}")
    public ResponseEntity<ApiResponse<?>> deletePost(
            @PathVariable Long postSeq,
            HttpServletRequest request
    ) {
        String token = jwtProvider.resolveToken(request.getHeader("Authorization"));
        if (token == null || !jwtProvider.validateToken(token)) {
            return ResponseEntity.status(401).body(
                    ApiResponse.of(401, "유효하지 않은 토큰입니다."));
        }

        String userId = jwtProvider.getUserIdFromToken(token);
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(
                    ApiResponse.of(404, "사용자를 찾을 수 없습니다."));
        }

        Post post = postRepository.findById(postSeq).orElse(null);
        if (post == null) {
            return ResponseEntity.status(404).body(
                    ApiResponse.of(404, "해당 게시글을 찾을 수 없습니다."));
        }

        if (!post.getAuthor().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(
                    ApiResponse.of(403, "삭제 권한이 없습니다."));
        }

        postRepository.delete(post);
        return ResponseEntity.ok(
                ApiResponse.of(200, "게시글 삭제 완료"));
    }
}
