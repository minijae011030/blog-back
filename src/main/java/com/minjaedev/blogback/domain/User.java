package com.minjaedev.blogback.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String email;


    private String password;
    private String githubId;
    private String instagramId;
    private String intro;
    private String personalUrl;
    private String profileImage;
}
