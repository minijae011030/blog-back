package com.minjaedev.blogback.util;

import com.minjaedev.blogback.domain.Category;
import com.minjaedev.blogback.domain.Post;
import com.minjaedev.blogback.domain.Tag;
import com.minjaedev.blogback.domain.User;
import com.minjaedev.blogback.exception.NotFoundException;
import com.minjaedev.blogback.exception.UnauthorizedException;
import com.minjaedev.blogback.repository.CategoryRepository;
import com.minjaedev.blogback.repository.PostRepository;
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
    private final PostRepository postRepository;

    public Category findOrCreateCategory(String name, User user) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("카테고리 이름이 비어 있을 수 없습니다.");
        }

        return categoryRepository.findByNameAndUser(name, user)
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .name(name)
                        .user(user)
                        .build()));
    }

    public List<Tag> resolveTags(List<String> tagNames, User user) {
        return new ArrayList<>(tagNames.stream()
                .distinct()
                .map(tagName -> tagRepository.findByNameAndUser(tagName, user)
                        .orElseGet(() -> tagRepository.save(Tag.builder()
                                .name(tagName)
                                .user(user)
                                .build())))
                .toList());
    }

    public Post getUserOwnedPost(Long postSeq, User user) {
        Post post = postRepository.findById(postSeq)
                .orElseThrow(() -> new NotFoundException("게시글을 찾을 수 없습니다."));

        if (!post.getAuthor().getId().equals(user.getId())) {
            throw new UnauthorizedException("접근 권한이 없습니다.");
        }

        return post;
    }
}
