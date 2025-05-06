package com.minjaedev.blogback.repository;

import com.minjaedev.blogback.domain.Category;
import com.minjaedev.blogback.domain.Post;
import com.minjaedev.blogback.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByAuthorAndIsPinnedTrueAndIsArchivedFalse(User author, Pageable pageable);
    Page<Post> findByAuthorAndIsArchivedTrue(User author, Pageable pageable);
    Page<Post> findAllByAuthorAndIsArchivedFalse(User author, Pageable pageable);
    Page<Post> findAllByAuthorAndCategoryAndIsArchivedFalse(User author, Category category, Pageable pageable);
    Page<Post> findAllByAuthorAndTags_NameAndIsArchivedFalse(User author, String tagName, Pageable pageable);
    List<Post> findTop5ByIsArchivedFalseOrderByViewsDesc();
    Page<Post> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
            String titleKeyword, String contentKeyword, Pageable pageable);
    int countByCategory(Category category);

    @Query("SELECT DISTINCT FUNCTION('DAY', p.createdAt) FROM Post p " +
            "WHERE p.author = :author AND p.isArchived = false AND p.createdAt BETWEEN :start AND :end")
    List<Integer> findPostDaysByMonth(User author, LocalDateTime start, LocalDateTime end);
}
