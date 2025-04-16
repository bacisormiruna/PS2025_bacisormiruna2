package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments", indexes = {
        @Index(name = "idx_comment_post", columnList = "post_id"),
        @Index(name = "idx_comment_created", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private String username;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @ManyToOne(fetch = FetchType.LAZY) // LAZY este mai eficient
    @JoinColumn(name = "post_id", nullable = false)
    @JsonIgnore
    private Post post;

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }}
