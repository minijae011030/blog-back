package com.minjaedev.blogback.repository;

import com.minjaedev.blogback.domain.Tag;
import com.minjaedev.blogback.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface
TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findAllByUser(User user);
    Optional<Tag> findByNameAndUser(String name, User user);
}
