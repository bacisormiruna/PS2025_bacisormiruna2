package com.example.demo.dto.commentdto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreateDTO {
    @NotBlank(message = "Content cannot be blank")
    @Size(max = 1000, message = "Content must be less than 1000 characters")
    private String content;

    @NotNull(message = "Post ID cannot be null")
    private Long postId;

    private String imageUrl;
}