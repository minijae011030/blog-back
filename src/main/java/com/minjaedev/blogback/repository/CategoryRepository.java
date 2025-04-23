package com.minjaedev.blogback.repository;

import com.minjaedev.blogback.domain.Category;
import com.minjaedev.blogback.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
    List<Category> findAllByUser(User user);

    Optional<Category> findByNameAndUser(String name, User user);
}
