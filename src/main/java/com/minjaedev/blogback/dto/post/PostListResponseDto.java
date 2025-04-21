package com.minjaedev.blogback.dto.post;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PostListResponseDto {
    private final int totalCount;
    private final List<PostResponseDto> posts;
}
