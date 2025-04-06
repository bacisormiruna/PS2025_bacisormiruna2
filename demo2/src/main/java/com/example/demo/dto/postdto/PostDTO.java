package com.example.demo.dto.postdto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDTO {
    private Long id;
    private Long authorId;
    private String content;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<String> hashtags;
    private Integer commentCount;

    public PostDTO(Long id, Long authorId, String content, String imageUrl, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id=id;
        this.authorId= authorId;
        this.content=content;
        this.imageUrl=imageUrl;
        this.createdAt=createdAt;
        this.updatedAt=updatedAt;
    }
}