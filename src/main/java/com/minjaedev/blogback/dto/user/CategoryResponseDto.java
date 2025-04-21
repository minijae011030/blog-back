package com.minjaedev.blogback.dto.user;

import com.minjaedev.blogback.domain.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CategoryResponseDto {
    private Long categoryId;
    private String name;
    private int postCount;

    public CategoryResponseDto(Category category, int postCount) {
        this.categoryId = category.getCategoryId();
        this.name = category.getName();
        this.postCount = postCount;
    }

}
