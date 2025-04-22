package com.minjaedev.blogback.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @ManyToMany
    @JoinTable(
            name = "post_tag",
            joinColumns = @JoinColumn(name = "post_seq"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private  Category category;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User author;

    @Column(nullable = false)
    private boolean isPinned = false;

    @Column(name = "is_archived", nullable = false)
    private boolean isArchived = false;

    @Column(nullable = false)
    private int views = 0;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}