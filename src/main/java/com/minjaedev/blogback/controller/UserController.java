package com.minjaedev.blogback.controller;

import com.minjaedev.blogback.common.ApiResponse;
import com.minjaedev.blogback.dto.user.UserUpdateRequestDto;

import com.minjaedev.blogback.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/account")
    public ResponseEntity<ApiResponse<?>> getMyInfo(@RequestHeader String blogId) {
        try {
            return ResponseEntity.ok(ApiResponse.of(200, "사용자 정보 조회 성공", userService.getUserInfo(blogId)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(ApiResponse.of(404, e.getMessage()));
        }
    }

    @PatchMapping("/update")
    public ResponseEntity<ApiResponse<?>> updateUserInfo(
            HttpServletRequest request,
            @RequestBody UserUpdateRequestDto requestDto
    ) {
        try {
            return ResponseEntity.ok(ApiResponse.of(200, "회원 정보 수정 성공", userService.updateUserInfo(request, requestDto)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(ApiResponse.of(401, e.getMessage()));
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<?>> getCategories(@RequestHeader String blogId) {
        try {
            return ResponseEntity.ok(ApiResponse.of(200, "카테고리 조회 성공", userService.getCategoryList(blogId)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(ApiResponse.of(404, e.getMessage()));
        }
    }

    @GetMapping("/tags")
    public ResponseEntity<ApiResponse<?>> getTags(@RequestHeader String blogId) {
        try {
            return ResponseEntity.ok(ApiResponse.of(200, "유저 태그 조회 성공", userService.getTagList(blogId)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(ApiResponse.of(404, e.getMessage()));
        }
    }
}
