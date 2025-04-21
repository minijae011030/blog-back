package com.minjaedev.blogback.repository;

import com.minjaedev.blogback.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, String> {
    Optional<Post> findByPostSeq(Long postSeq);
}
