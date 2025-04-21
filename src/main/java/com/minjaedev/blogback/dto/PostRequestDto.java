package com.minjaedev.blogback.dto;

import lombok.Getter;

import java.util.Date;
import java.util.List;

@Getter
public class PostRequestDto {
    private String title;
    private String category;
    private List<String> tags;
    private String content;
}
