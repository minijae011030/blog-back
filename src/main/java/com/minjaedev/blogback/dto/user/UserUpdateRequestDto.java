package com.minjaedev.blogback.dto.user;

import lombok.Getter;

@Getter
public class UserUpdateRequestDto {
    private String name;
    private String intro;
    private String githubId;
    private String instagramId;
    private String personalUrl;
    private String profileImage;
}
