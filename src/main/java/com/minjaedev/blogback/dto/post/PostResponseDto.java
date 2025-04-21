package com.minjaedev.blogback.dto.post;

import com.minjaedev.blogback.domain.Post;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class PostResponseDto {
    private final String title;
    private final String content;
    private final String category;
    private final List<String> tags;
    private final LocalDateTime createdAt;
    private final String authorName;

    public PostResponseDto(Post post) {
        this.title = post.getTitle();
        this.content = post.getContent();
        this.category = post.getCategory();
        this.tags = post.getTags();
        this.createdAt = post.getCreatedAt();
        this.authorName = post.getAuthor().getName();
    }
}
