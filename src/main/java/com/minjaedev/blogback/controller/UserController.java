package com.minjaedev.blogback.controller;

import com.minjaedev.blogback.common.ApiResponse;
import com.minjaedev.blogback.domain.Category;
import com.minjaedev.blogback.domain.User;
import com.minjaedev.blogback.dto.user.CategoryResponseDto;
import com.minjaedev.blogback.dto.user.TagResponseDto;
import com.minjaedev.blogback.dto.user.UserResponseDto;
import com.minjaedev.blogback.repository.CategoryRepository;
import com.minjaedev.blogback.repository.PostRepository;
import com.minjaedev.blogback.repository.TagRepository;
import com.minjaedev.blogback.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final PostRepository postRepository;

    @GetMapping("/account")
    public ResponseEntity<ApiResponse<?>> getMyInfo(
            @RequestHeader String blogId) {
        User user = userRepository.findByBlogId(blogId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(
                    ApiResponse.of(404, "존재하지 않는 회원입니다."));
        }

        return ResponseEntity.ok(
                ApiResponse.of(200, "사용자 정보 조회 성공", new UserResponseDto(user))
        );
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<?>> getCategories(@RequestHeader String blogId) {
        User user = userRepository.findByBlogId(blogId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(
                    ApiResponse.of(404, "존재하지 않는 블로그입니다."));
        }

        List<Category> categories = categoryRepository.findAllByUser(user);
        List<CategoryResponseDto> result = categories.stream()
                .map(category -> new CategoryResponseDto(
                        category,
                        postRepository.countByCategory(category)))
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.of(200, "카테고리 조회 성공", result));
    }

    @GetMapping("/tags")
    public ResponseEntity<ApiResponse<?>> getTags(
            @RequestHeader String blogId
    ) {
        User user = userRepository.findByBlogId(blogId).orElse(null);

        if (user == null) {
            return ResponseEntity.status(404).body(
                    ApiResponse.of(404, "존재하지 않는 블로그입니다."));
        }

        List<TagResponseDto> tags = tagRepository.findAllByUser(user).stream()
                .map(TagResponseDto::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.of(200, "유저 태그 조회 성공", tags));
    }
}
