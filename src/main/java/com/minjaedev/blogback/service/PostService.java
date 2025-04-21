package com.minjaedev.blogback.service;

import com.minjaedev.blogback.common.ApiResponse;
import com.minjaedev.blogback.domain.*;
import com.minjaedev.blogback.dto.post.PostCreateResponseDto;
import com.minjaedev.blogback.dto.post.PostListResponseDto;
import com.minjaedev.blogback.dto.post.PostRequestDto;
import com.minjaedev.blogback.dto.post.PostResponseDto;
import com.minjaedev.blogback.jwt.JwtProvider;
import com.minjaedev.blogback.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    public ResponseEntity<ApiResponse<?>> getPostBySeq(Long postSeq) {
        Post post = postRepository.findById(postSeq)
                .orElse(null);

        if (post == null) {
            return ResponseEntity.status(404).body(ApiResponse.of(404, "해당 게시글을 찾을 수 없습니다."));
        }

        return ResponseEntity.ok(ApiResponse.of(200, "게시글 조회 성공", new PostResponseDto(post)));
    }

    public ResponseEntity<ApiResponse<?>> getPosts(String blogId, int page, int size, String category, String tag) {
        User user = getUserByBlogId(blogId);

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> postPage;

        if (category != null) {
            Category categoryEntity = categoryRepository.findByNameAndUser(category, user)
                    .orElseThrow(() -> new RuntimeException("해당 카테고리를 찾을 수 없습니다."));
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
        return ResponseEntity.ok(ApiResponse.of(200, "게시글 목록 조회 성공", response));
    }

    public ResponseEntity<ApiResponse<?>> getPinnedPosts(String blogId, int page, int size) {
        User user = getUserByBlogId(blogId);

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> pinnedPage = postRepository.findByAuthorAndIsPinnedTrue(user, pageable);

        List<PostResponseDto> postDtos = pinnedPage.getContent().stream()
                .map(PostResponseDto::new)
                .toList();

        PostListResponseDto response = new PostListResponseDto((int) pinnedPage.getTotalElements(), postDtos);
        return ResponseEntity.ok(ApiResponse.of(200, "고정 게시글 조회 성공", response));
    }

    public ResponseEntity<ApiResponse<?>> createPost(PostRequestDto requestDto, HttpServletRequest request) {
        User user = getAuthenticatedUser(request);
        Category category = findOrCreateCategory(requestDto.getCategory(), user);
        List<Tag> tagList = resolveTags(requestDto.getTags(), user);

        Post newPost = Post.builder()
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .author(user)
                .category(category)
                .tags(tagList)
                .build();

        Post savedPost = postRepository.save(newPost);
        return ResponseEntity.ok(ApiResponse.of(200, "게시글 작성 완료", new PostCreateResponseDto(savedPost.getPostSeq())));
    }

    public ResponseEntity<ApiResponse<?>> updatePost(Long postSeq, PostRequestDto requestDto, HttpServletRequest request) {
        User user = getAuthenticatedUser(request);
        Post post = getUserOwnedPost(postSeq, user);
        Category category = findOrCreateCategory(requestDto.getCategory(), user);
        List<Tag> tagList = resolveTags(requestDto.getTags(), user);

        post.setTitle(requestDto.getTitle());
        post.setContent(requestDto.getContent());
        post.setCategory(category);
        post.setTags(tagList);

        postRepository.save(post);
        return ResponseEntity.ok(ApiResponse.of(200, "게시글 수정 완료"));
    }

    public ResponseEntity<ApiResponse<?>> deletePost(Long postSeq, HttpServletRequest request) {
        User user = getAuthenticatedUser(request);
        Post post = getUserOwnedPost(postSeq, user);

        postRepository.delete(post);
        return ResponseEntity.ok(ApiResponse.of(200, "게시글 삭제 완료"));
    }

    public ResponseEntity<ApiResponse<?>> setPinned(Long postSeq, HttpServletRequest request, boolean pin) {
        User user = getAuthenticatedUser(request);
        Post post = getUserOwnedPost(postSeq, user);

        post.setPinned(pin);
        postRepository.save(post);

        return ResponseEntity.ok(ApiResponse.of(200, pin ? "게시글 고정 완료" : "게시글 고정 해제 완료"));
    }

    // ======= 유틸 =======

    public User getAuthenticatedUser(HttpServletRequest request) {
        String token = jwtProvider.resolveToken(request.getHeader("Authorization"));
        if (token == null || !jwtProvider.validateToken(token)) {
            throw new RuntimeException("유효하지 않은 토큰입니다.");
        }

        String userId = jwtProvider.getUserIdFromToken(token);
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    public Post getUserOwnedPost(Long postSeq, User user) {
        Post post = postRepository.findById(postSeq)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        if (!post.getAuthor().getId().equals(user.getId())) {
            throw new RuntimeException("접근 권한이 없습니다.");
        }

        return post;
    }

    public User getUserByBlogId(String blogId) {
        return userRepository.findByBlogId(blogId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 회원입니다."));
    }

    private Category findOrCreateCategory(String name, User user) {
        return categoryRepository.findByNameAndUser(name, user)
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .name(name)
                        .user(user)
                        .build()));
    }

    private List<Tag> resolveTags(List<String> tagNames, User user) {
        List<Tag> tagList = new ArrayList<>();
        for (String tagName : tagNames) {
            Tag tag = tagRepository.findByNameAndUser(tagName, user)
                    .orElseGet(() -> tagRepository.save(Tag.builder()
                            .name(tagName)
                            .user(user)
                            .build()));
            tagList.add(tag);
        }
        return tagList;
    }
}