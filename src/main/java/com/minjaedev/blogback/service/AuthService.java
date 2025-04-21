package com.minjaedev.blogback.service;

import com.minjaedev.blogback.domain.User;
import com.minjaedev.blogback.dto.auth.LoginRequestDto;
import com.minjaedev.blogback.dto.auth.SignupRequestDto;
import com.minjaedev.blogback.jwt.JwtProvider;
import com.minjaedev.blogback.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public void signup(SignupRequestDto req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .blogId(req.getBlogId())
                .build();

        userRepository.save(user);
    }

    public String login(LoginRequestDto req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        return JwtProvider.TOKEN_PREFIX + jwtProvider.generateToken(user.getId());
    }
}