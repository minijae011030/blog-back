package com.minjaedev.blogback.util;

import com.minjaedev.blogback.domain.User;
import com.minjaedev.blogback.exception.NotFoundException;
import com.minjaedev.blogback.exception.UnauthorizedException;
import com.minjaedev.blogback.jwt.JwtProvider;
import com.minjaedev.blogback.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthUtil {
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    public User getAuthenticatedUser(HttpServletRequest request) {
        String token = jwtProvider.resolveToken(request.getHeader("Authorization"));
        if (token == null || !jwtProvider.validateToken(token)) {
            throw new UnauthorizedException("유효하지 않은 토큰입니다.");
        }

        String userId = jwtProvider.getUserIdFromToken(token);
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
    }

    public User getUserByBlogId(String blogId) {
        return userRepository.findByBlogId(blogId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다."));
    }
}
