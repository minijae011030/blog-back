package com.minjaedev.blogback.controller;

import com.minjaedev.blogback.common.ApiResponse;
import com.minjaedev.blogback.domain.User;
import com.minjaedev.blogback.dto.user.UserResponseDto;
import com.minjaedev.blogback.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;

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
}
