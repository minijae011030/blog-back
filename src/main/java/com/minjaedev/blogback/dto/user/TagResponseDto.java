package com.minjaedev.blogback.dto.user;

import com.minjaedev.blogback.domain.Tag;
import lombok.Getter;

@Getter
public class TagResponseDto {
    private final Long tagId;
    private final String name;
    private final int postCount;


    public TagResponseDto(Tag tag) {
        this.tagId = tag.getId();
        this.name = tag.getName();
        this.postCount = tag.getPosts().size();
    }
}
