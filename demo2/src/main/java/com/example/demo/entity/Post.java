package com.example.demo.entity;

import com.example.demo.dto.commentdto.CommentDTO;
import com.example.demo.dto.hashtagdto.HashtagDTO;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "content")
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_public")
    private Boolean isPublic = true;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Lob
    @Column(name = "image",columnDefinition = "LONGBLOB")
    private byte[] image;

    @Column(nullable = false)
    private String username;

    @ManyToMany(cascade = {CascadeType.ALL})
    @JoinTable(
            name = "post_hashtag",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "hashtag_id")
    )
    private Set<Hashtag> hashtags = new HashSet<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    @PreRemove
    private void removeHashtagsFromPosts() {
        for (Hashtag hashtag : hashtags) {
            hashtag.getPosts().remove(this);
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}