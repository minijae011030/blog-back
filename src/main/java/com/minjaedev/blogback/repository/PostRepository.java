package com.minjaedev.blogback.repository;

import com.minjaedev.blogback.domain.Post;
import com.minjaedev.blogback.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, String> {
    Optional<Post> findByPostSeq(Long postSeq);
    Page<Post> findAllByAuthor(User author, Pageable pageable);
}
