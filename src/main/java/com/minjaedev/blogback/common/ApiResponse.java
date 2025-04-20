package com.minjaedev.blogback.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private int status;
    private String message;
    private T data;

    // 메시지만 보내는 경우를 위한 팩토리 메서드
    public static <T> ApiResponse<T> of(int statusCode, String message) {
        return new ApiResponse<>(statusCode, message, null);
    }

    // 메시지 + result 객체
    public static <T> ApiResponse<T> of(int statusCode, String message, T data) {
        return new ApiResponse<>(statusCode, message, data);
    }
}
