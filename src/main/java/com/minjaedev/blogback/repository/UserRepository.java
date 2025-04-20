package com.minjaedev.blogback.repository;

import com.minjaedev.blogback.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);   // 이메일로 사용자 조회
    boolean existsByEmail(String email);        // 이메일 중복 여부 확인
}
