package com.example.demo.dto.commentdto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
    private Long id;
    private String content;
    private String imageUrl;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private String username;
    private Long authorId;
    private Long postId;

    public CommentDTO(Long id, Long authorId, String username, String content, LocalDateTime createdAt) {
        this.id = id;
        this.authorId = authorId;
        this.username = username;
        this.content = content;
        this.createdAt = createdAt;
    }
}
