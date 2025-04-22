package com.minjaedev.blogback.service;

import com.minjaedev.blogback.domain.User;
import com.minjaedev.blogback.dto.auth.LoginRequestDto;
import com.minjaedev.blogback.dto.auth.SignupRequestDto;
import com.minjaedev.blogback.exception.NotFoundException;
import com.minjaedev.blogback.exception.UnauthorizedException;
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
            throw new UnauthorizedException("이미 존재하는 이메일입니다.");
        }

        if (userRepository.existsByBlogId(req.getBlogId())) {
            throw new UnauthorizedException("이미 사용 중인 블로그 ID입니다.");
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
                .orElseThrow(() -> new NotFoundException("이메일이 존재하지 않습니다."));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("비밀번호가 일치하지 않습니다.");
        }

        return jwtProvider.generateToken(user.getId());
    }
}