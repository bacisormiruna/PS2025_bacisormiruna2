package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    private String imageUrl;

    @Column(nullable = false)
    private LocalDateTime publishTime; // = createdAt în DTO

    @Column
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private Long userId; // Dacă vrei să ții și id-ul userului (opțional)

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;
}
