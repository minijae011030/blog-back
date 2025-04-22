package com.minjaedev.blogback.exception;

import com.minjaedev.blogback.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNotFound(NotFoundException e) {
        log.warn("❗ [NOT_FOUND] ➤ {}", e.getMessage());
        return ResponseEntity.status(404).body(ApiResponse.of(404, e.getMessage()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<?>> handleUnauthorized(UnauthorizedException e) {
        log.warn("❗ [UNAUTHORIZED] ➤ {}", e.getMessage());
        return ResponseEntity.status(401).body(ApiResponse.of(401, e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleBadRequest(IllegalArgumentException e) {
        log.warn("❗ [BAD_REQUEST] ➤ {}", e.getMessage());
        return ResponseEntity.status(400).body(ApiResponse.of(400, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleServerError(Exception e) {
        log.error("❗ [INTERNAL_ERROR] ➤ {}", e.getMessage(), e);
        return ResponseEntity.status(500).body(ApiResponse.of(500, "서버 오류가 발생했습니다: " + e.getMessage()));
    }
}