package com.example.demo.dto.commentdto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


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