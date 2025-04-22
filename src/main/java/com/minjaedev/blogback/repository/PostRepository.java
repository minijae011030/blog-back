package com.minjaedev.blogback.repository;

import com.minjaedev.blogback.domain.Category;
import com.minjaedev.blogback.domain.Post;
import com.minjaedev.blogback.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Optional<Post> findByPostSeq(Long postSeq);
    Page<Post> findAllByAuthor(User author, Pageable pageable);
    Page<Post> findAllByAuthorAndCategory(User author, Category category, Pageable pageable);
    Page<Post> findAllByAuthorAndTags_Name(User author, String tagName, Pageable pageable);
    Page<Post> findByAuthorAndIsPinnedTrue(User user, Pageable pageable);
    Page<Post> findByAuthorAndIsArchivedTrue(User author, Pageable pageable);
    Page<Post> findByAuthorAndIsArchivedFalse(User author, Pageable pageable);

    int countByCategory(Category category);
}
