package com.minjaedev.blogback.service;

import com.minjaedev.blogback.common.ApiResponse;
import com.minjaedev.blogback.domain.*;
import com.minjaedev.blogback.dto.post.PostCreateResponseDto;
import com.minjaedev.blogback.dto.post.PostListResponseDto;
import com.minjaedev.blogback.dto.post.PostRequestDto;
import com.minjaedev.blogback.dto.post.PostResponseDto;
import com.minjaedev.blogback.exception.NotFoundException;
import com.minjaedev.blogback.exception.UnauthorizedException;
import com.minjaedev.blogback.repository.*;
import com.minjaedev.blogback.util.AuthUtil;
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
    private final AuthUtil authUtil;
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    public ResponseEntity<ApiResponse<?>> getPostBySeq(Long postSeq, HttpServletRequest request) {
        Post post = postRepository.findById(postSeq)
                .orElseThrow(() -> new NotFoundException("해당 게시글을 찾을 수 없습니다."));

        if (post.isArchived()) {
            User user = authUtil.getAuthenticatedUser(request);
            if (!post.getAuthor().getId().equals(user.getId())) {
                throw  new UnauthorizedException("보관 게시글에 접근할 수 없습니다.");
            }
        }
        return ResponseEntity.ok(ApiResponse.of(200, "게시글 조회 성공", new PostResponseDto(post)));
    }

    public ResponseEntity<ApiResponse<?>> getPosts(String blogId, int page, int size, String category, String tag) {
        User user = authUtil.getUserByBlogId(blogId);

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> postPage;

        if (category != null) {
            Category categoryEntity = categoryRepository.findByNameAndUser(category, user)
                    .orElseThrow(() -> new NotFoundException("해당 카테고리를 찾을 수 없습니다."));
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
        User user = authUtil.getUserByBlogId(blogId);
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> pinnedPage = postRepository.findByAuthorAndIsPinnedTrue(user, pageable);

        List<PostResponseDto> postDtos = pinnedPage.getContent().stream()
                .map(PostResponseDto::new)
                .toList();

        PostListResponseDto response = new PostListResponseDto((int) pinnedPage.getTotalElements(), postDtos);
        return ResponseEntity.ok(ApiResponse.of(200, "고정 게시글 조회 성공", response));
    }

    public ResponseEntity<ApiResponse<?>> createPost(PostRequestDto requestDto, HttpServletRequest request) {
        User user = authUtil.getAuthenticatedUser(request);
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
        User user = authUtil.getAuthenticatedUser(request);
        Post post = authUtil.getUserOwnedPost(postSeq, user);
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
        User user = authUtil.getAuthenticatedUser(request);
        Post post = authUtil.getUserOwnedPost(postSeq, user);

        postRepository.delete(post);
        return ResponseEntity.ok(ApiResponse.of(200, "게시글 삭제 완료"));
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

    public ResponseEntity<ApiResponse<?>> setPinned(Long postSeq, HttpServletRequest request, boolean pin) {
        User user = authUtil.getAuthenticatedUser(request);
        Post post = authUtil.getUserOwnedPost(postSeq, user);

        post.setPinned(pin);
        postRepository.save(post);

        return ResponseEntity.ok(ApiResponse.of(200, pin ? "게시글 고정 완료" : "게시글 고정 해제 완료"));
    }

    public ResponseEntity<ApiResponse<?>> getArchivedPosts( HttpServletRequest request, int page, int size) {
        User user = authUtil.getAuthenticatedUser(request);
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Post> archivedPage = postRepository.findByAuthorAndIsArchivedTrue(user, pageable);
        List<PostResponseDto> postDtos = archivedPage.getContent().stream()
                .map(PostResponseDto::new)
                .toList();

        PostListResponseDto response = new PostListResponseDto((int) archivedPage.getTotalElements(), postDtos);
        return ResponseEntity.ok(
                ApiResponse.of(200, "보관된 게시글 조회 성공", response));
    }

    public ResponseEntity<ApiResponse<?>> setArchived(Long postSeq, HttpServletRequest request, boolean archiv) {
        User user = authUtil.getAuthenticatedUser(request);
        Post post = authUtil.getUserOwnedPost(postSeq, user);
        post.setArchived(archiv);
        postRepository.save(post);
        return ResponseEntity.ok(
                ApiResponse.of(200, archiv ? "게시글 보관 완료" : "게시글 복원 완료"));
    }
}