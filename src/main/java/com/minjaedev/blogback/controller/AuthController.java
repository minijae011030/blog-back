package com.minjaedev.blogback.controller;

import com.minjaedev.blogback.common.ApiResponse;
import com.minjaedev.blogback.domain.User;
import com.minjaedev.blogback.dto.LoginRequestDto;
import com.minjaedev.blogback.dto.SignupRequestDto;
import com.minjaedev.blogback.jwt.JwtProvider;
import com.minjaedev.blogback.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<?>> signup(@RequestBody SignupRequestDto reqeust) {
        if (userRepository.existsByEmail(reqeust.getEmail())) {
            return ResponseEntity.badRequest().body(ApiResponse.of(400, "Email already exists"));
        }

        User newUser = User.builder()
                .name(reqeust.getName())
                .email(reqeust.getEmail())
                .password(passwordEncoder.encode(reqeust.getPassword()))
                .build();

        userRepository.save(newUser);
        return ResponseEntity.ok(ApiResponse.of(200, "User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@RequestBody LoginRequestDto reqeust) {
        User user = userRepository.findByEmail(reqeust.getEmail())
                .orElse(null);

        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.of(401, "Invalid email"));
        }

        if (!passwordEncoder.matches(reqeust.getPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body(ApiResponse.of(401, "Invalid password"));
        }

        String token = JwtProvider.TOKEN_PREFIX + jwtProvider.generateToken(user.getId());

        return ResponseEntity.ok(
                ApiResponse.of(200, "login successful", token)
        );
    }
}
