package com.minjaedev.blogback.dto.post;

import lombok.Getter;

import java.util.List;

@Getter
public class PostRequestDto {
    private String title;
    private String category;
    private List<String> tags;
    private String content;
}
