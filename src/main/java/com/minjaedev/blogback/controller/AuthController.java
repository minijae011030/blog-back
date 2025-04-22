package com.minjaedev.blogback.controller;

import com.minjaedev.blogback.common.ApiResponse;
import com.minjaedev.blogback.dto.auth.LoginRequestDto;
import com.minjaedev.blogback.dto.auth.SignupRequestDto;
import com.minjaedev.blogback.service.AuthService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<?>> signup(@RequestBody SignupRequestDto request) {
        try {
            authService.signup(request);
            return ResponseEntity.ok(ApiResponse.of(200, "회원가입이 성공적으로 완료되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.of(400, e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@RequestBody LoginRequestDto request) {
        try {
            String token = authService.login(request);
            return ResponseEntity.ok(ApiResponse.of(200, "로그인이 완료되었습니다.", token));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.of(401, e.getMessage()));
        }
    }
}
