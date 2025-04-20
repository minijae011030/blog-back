package com.minjaedev.blogback.controller;

import com.minjaedev.blogback.common.ApiResponse;
import com.minjaedev.blogback.domain.User;
import com.minjaedev.blogback.dto.UserResponseDto;
import com.minjaedev.blogback.jwt.JwtProvider;
import com.minjaedev.blogback.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<?>> getMyInfo(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String token = jwtProvider.resolveToken(authHeader);

        if (token == null || !jwtProvider.validateToken(token)) {
            return ResponseEntity
                    .status(401)
                    .body(ApiResponse.of(401, "유효하지 않은 토큰입니다."));
        }

        String userId = jwtProvider.getUserIdFromToken(token);
        User user = userRepository.findById(userId).orElse(null);


        if (user == null) {
            return ResponseEntity
                    .status(404)
                    .body(ApiResponse.of(404, "사용자를 찾을 수 없습니다."));
        }

        return ResponseEntity.ok(
                ApiResponse.of(200, "사용자 정보 조회 성공", new UserResponseDto(user))
        );
    }
}
