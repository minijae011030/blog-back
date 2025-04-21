package com.minjaedev.blogback.dto.user;

import com.minjaedev.blogback.domain.User;
import lombok.Getter;

@Getter
public class UserResponseDto {
    private final String id;
    private final String name;
    private final String email;
    private final String githubId;
    private final String instagramId;
    private final String intro;
    private final String profileImage;

    public UserResponseDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.githubId = user.getGithubId();
        this.instagramId = user.getInstagramId();
        this.intro = user.getIntro();
        this.profileImage = user.getProfileImage();
    }
}
