package com.minjaedev.blogback.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postSeq;

    @Column(nullable = false)
    private String title;

    private LocalDateTime createdAt;

    @ElementCollection
    private List<String> tags;

    private String category;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User author;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

}
