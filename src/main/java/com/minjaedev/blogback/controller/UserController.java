package com.minjaedev.blogback.controller;

import com.minjaedev.blogback.common.ApiResponse;
import com.minjaedev.blogback.domain.Category;
import com.minjaedev.blogback.domain.User;
import com.minjaedev.blogback.dto.user.CategoryResponseDto;
import com.minjaedev.blogback.dto.user.TagResponseDto;
import com.minjaedev.blogback.dto.user.UserResponseDto;
import com.minjaedev.blogback.dto.user.UserUpdateRequestDto;
import com.minjaedev.blogback.jwt.JwtProvider;
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
    private final JwtProvider jwtProvider;

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

    @PatchMapping("/update")
    public ResponseEntity<ApiResponse<?>> updateUserInfo(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody UserUpdateRequestDto requestDto
    ) {
        String token = jwtProvider.resolveToken(authHeader);
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

        if (requestDto.getName() != null) user.setName(requestDto.getName());
        if (requestDto.getIntro() != null) user.setIntro(requestDto.getIntro());
        if (requestDto.getGithubId() != null) user.setGithubId(requestDto.getGithubId());
        if (requestDto.getInstagramId() != null) user.setInstagramId(requestDto.getInstagramId());
        if (requestDto.getPersonalUrl() != null) user.setPersonalUrl(requestDto.getPersonalUrl());
        if (requestDto.getProfileImage() != null) user.setProfileImage(requestDto.getProfileImage());

        userRepository.save(user);

        return ResponseEntity.ok(
                ApiResponse.of(200, "회원 정보 수정 성공", new UserResponseDto(user)));
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
