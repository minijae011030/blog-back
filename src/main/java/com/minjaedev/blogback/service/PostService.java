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
import com.minjaedev.blogback.util.PostUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final AuthUtil authUtil;
    private final PostUtil postUtil;
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;

    public ResponseEntity<ApiResponse<?>> getPostBySeq(Long postSeq, HttpServletRequest request) {
        Post post = postRepository.findById(postSeq)
                .orElseThrow(() -> new NotFoundException("해당 게시글을 찾을 수 없습니다."));

        if (post.isArchived()) {
            User user = authUtil.getAuthenticatedUser(request);
            if (!post.getAuthor().getId().equals(user.getId())) {
                throw  new UnauthorizedException("보관 게시글에 접근할 수 없습니다.");
            }
        }

        HttpSession session = request.getSession();
        String viewKey = "VIEWED_POST_" + postSeq;

        if (session.getAttribute(viewKey) == null) {
            post.setViews(post.getViews() + 1);
            postRepository.save(post);
            session.setAttribute(viewKey, post);
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
            postPage = postRepository.findAllByAuthorAndCategoryAndIsArchivedFalse(user, categoryEntity, pageable);
        } else if (tag != null) {
            postPage = postRepository.findAllByAuthorAndTags_NameAndIsArchivedFalse(user, tag, pageable);
        } else {
            postPage = postRepository.findAllByAuthorAndIsArchivedFalse(user, pageable);
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

    public ResponseEntity<ApiResponse<?>> getPopularPosts() {
        List<Post> posts = postRepository.findTop5ByIsArchivedFalseOrderByViewsDesc();

        List<PostResponseDto> responseDtos = posts.stream()
                .map(PostResponseDto::new)
                .toList();

        return ResponseEntity.ok(ApiResponse.of(200, "인기 게시글 조회 성공", responseDtos));
    }

    public ResponseEntity<ApiResponse<?>> createPost(PostRequestDto requestDto, HttpServletRequest request) {
        User user = authUtil.getAuthenticatedUser(request);
        Category category = postUtil.findOrCreateCategory(requestDto.getCategory(), user);
        List<Tag> tagList = postUtil.resolveTags(requestDto.getTags(), user);

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
        Post post = postUtil.getUserOwnedPost(postSeq, user);
        Category category = postUtil.findOrCreateCategory(requestDto.getCategory(), user);
        List<Tag> tagList = postUtil.resolveTags(requestDto.getTags(), user);

        post.setTitle(requestDto.getTitle());
        post.setContent(requestDto.getContent());
        post.setCategory(category);
        post.setTags(tagList);

        postRepository.save(post);
        return ResponseEntity.ok(ApiResponse.of(200, "게시글 수정 완료"));
    }

    public ResponseEntity<ApiResponse<?>> deletePost(Long postSeq, HttpServletRequest request) {
        User user = authUtil.getAuthenticatedUser(request);
        Post post = postUtil.getUserOwnedPost(postSeq, user);

        postRepository.delete(post);
        return ResponseEntity.ok(ApiResponse.of(200, "게시글 삭제 완료"));
    }

    public ResponseEntity<ApiResponse<?>> setPinned(Long postSeq, HttpServletRequest request, boolean pinned) {
        User user = authUtil.getAuthenticatedUser(request);
        Post post = postUtil.getUserOwnedPost(postSeq, user);

        post.setPinned(pinned);
        postRepository.save(post);

        return ResponseEntity.ok(ApiResponse.of(200, pinned ? "게시글 고정 완료" : "게시글 고정 해제 완료"));
    }

    public ResponseEntity<ApiResponse<?>> setArchived(Long postSeq, HttpServletRequest request, boolean archived) {
        User user = authUtil.getAuthenticatedUser(request);
        Post post = postUtil.getUserOwnedPost(postSeq, user);
        post.setArchived(archived);
        postRepository.save(post);
        return ResponseEntity.ok(
                ApiResponse.of(200, archived ? "게시글 보관 완료" : "게시글 복원 완료"));
    }

    public ResponseEntity<ApiResponse<?>> searchPosts(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> postPage = postRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
                keyword, keyword, pageable);

        List<PostResponseDto> postDtos = postPage.getContent().stream()
                .map(PostResponseDto::new)
                .toList();

        PostListResponseDto response = new PostListResponseDto((int) postPage.getTotalElements(), postDtos);
        return ResponseEntity.ok(ApiResponse.of(200, "검색 결과", response));
    }
}