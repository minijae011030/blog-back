package com.minjaedev.blogback.jwt;

import lombok.Getter;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.*;

import java.util.Date;

@Component
public class JwtProvider {
    private final String secretKey = "secretKey";
    private final long expirationMs = 1000 * 60 * 60;
    // prefix 제공자
    @Getter
    public static final String TOKEN_PREFIX = "Bearer ";

    // 토큰 생성
    public String generateToken(String userId) {
        return Jwts.builder()
                .setSubject(userId) // 토큰 안에 들어갈 내용 (보통 유저 ID)
                .setIssuedAt(new Date()) // 토큰 발행 시간
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs)) // 만료 시간
                .signWith(SignatureAlgorithm.HS256, secretKey) // 서명
                .compact();
    }

    // 토큰에서 userId 추출
    public String getUserIdFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // prefix 제거 유틸
    public String resolveToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }

    // 토큰 검증 함수
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
