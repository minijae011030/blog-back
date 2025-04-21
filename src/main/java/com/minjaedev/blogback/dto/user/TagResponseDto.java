package com.minjaedev.blogback.dto.user;

import com.minjaedev.blogback.domain.Tag;
import lombok.Getter;

@Getter
public class TagResponseDto {
    private Long tagId;
    private String name;

    public TagResponseDto(Tag tag) {
        this.tagId = tag.getId();
        this.name = tag.getName();
    }
}
