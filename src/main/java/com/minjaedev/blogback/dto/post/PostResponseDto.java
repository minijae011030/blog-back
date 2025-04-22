package com.minjaedev.blogback.dto.post;

import com.minjaedev.blogback.domain.Post;
import com.minjaedev.blogback.domain.Tag;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class PostResponseDto {
    private final String title;
    private final String content;
    private final String category;
    private final List<String> tags;
    private final LocalDateTime createdAt;
    private final String authorName;
    private final boolean isArchived;
    private final int views;

    public PostResponseDto(Post post) {
        this.title = post.getTitle();
        this.content = post.getContent();
        this.category = post.getCategory().getName();
        this.tags = post.getTags().stream()
                .map(Tag::getName)
                .collect(Collectors.toList());
        this.createdAt = post.getCreatedAt();
        this.authorName = post.getAuthor().getName();
        this.isArchived = post.isArchived();
        this.views = post.getViews();
    }
}
