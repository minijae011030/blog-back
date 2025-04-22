package com.minjaedev.blogback.service;

import com.minjaedev.blogback.domain.User;
import com.minjaedev.blogback.dto.user.CategoryResponseDto;
import com.minjaedev.blogback.dto.user.TagResponseDto;
import com.minjaedev.blogback.dto.user.UserResponseDto;
import com.minjaedev.blogback.dto.user.UserUpdateRequestDto;
import com.minjaedev.blogback.exception.NotFoundException;
import com.minjaedev.blogback.exception.UnauthorizedException;
import com.minjaedev.blogback.jwt.JwtProvider;
import com.minjaedev.blogback.repository.CategoryRepository;
import com.minjaedev.blogback.repository.PostRepository;
import com.minjaedev.blogback.repository.TagRepository;
import com.minjaedev.blogback.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final PostRepository postRepository;

    public User getUserByBlogId(String blogId) {
        return userRepository.findByBlogId(blogId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다."));
    }

    public User getAuthenticatedUser(HttpServletRequest request) {
        String token = jwtProvider.resolveToken(request.getHeader("Authorization"));
        if (token == null || !jwtProvider.validateToken(token)) {
            throw new UnauthorizedException("유효하지 않은 토큰입니다.");
        }

        String userId = jwtProvider.getUserIdFromToken(token);
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
    }

    public UserResponseDto getUserInfo(String blogId) {
        User user = getUserByBlogId(blogId);
        return UserResponseDto.of(user);
    }

    public UserResponseDto updateUserInfo(HttpServletRequest request, UserUpdateRequestDto dto) {
        User user = getAuthenticatedUser(request);

        if (dto.getName() != null) user.setName(dto.getName());
        if (dto.getIntro() != null) user.setIntro(dto.getIntro());
        if (dto.getGithubId() != null) user.setGithubId(dto.getGithubId());
        if (dto.getInstagramId() != null) user.setInstagramId(dto.getInstagramId());
        if (dto.getPersonalUrl() != null) user.setPersonalUrl(dto.getPersonalUrl());
        if (dto.getProfileImage() != null) user.setProfileImage(dto.getProfileImage());

        userRepository.save(user);
        return UserResponseDto.of(user);
    }

    public List<CategoryResponseDto> getCategoryList(String blogId) {
        User user = getUserByBlogId(blogId);

        return categoryRepository.findAllByUser(user)
                .stream()
                .map(category -> new CategoryResponseDto(
                        category,
                        postRepository.countByCategory(category)))
                .collect(Collectors.toList());
    }

    public List<TagResponseDto> getTagList(String blogId) {
        User user = getUserByBlogId(blogId);

        return tagRepository.findAllByUser(user)
                .stream()
                .map(TagResponseDto::new)
                .collect(Collectors.toList());
    }
}