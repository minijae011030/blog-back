package com.minjaedev.blogback.repository;

import com.minjaedev.blogback.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, String> {
}
