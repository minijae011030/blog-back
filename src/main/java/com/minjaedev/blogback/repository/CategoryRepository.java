package com.minjaedev.blogback.repository;

import com.minjaedev.blogback.domain.Category;
import com.minjaedev.blogback.domain.User;

import java.util.List;

public interface CategoryRepository {
    List<Category> findByUser(User user);
}
