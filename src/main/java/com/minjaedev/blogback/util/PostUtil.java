package com.minjaedev.blogback.util;

import com.minjaedev.blogback.domain.Category;
import com.minjaedev.blogback.domain.Tag;
import com.minjaedev.blogback.domain.User;
import com.minjaedev.blogback.repository.CategoryRepository;
import com.minjaedev.blogback.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PostUtil {
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    public Category findOrCreateCategory(String name, User user) {
        return categoryRepository.findByNameAndUser(name, user)
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .name(name)
                        .user(user)
                        .build()));
    }

    public List<Tag> resolveTags(List<String> tagNames, User user) {
        List<Tag> tagList = new ArrayList<>();
        for (String tagName : tagNames) {
            Tag tag = tagRepository.findByNameAndUser(tagName, user)
                    .orElseGet(() -> tagRepository.save(Tag.builder()
                            .name(tagName)
                            .user(user)
                            .build()));
            tagList.add(tag);
        }
        return tagList;
    }
}
