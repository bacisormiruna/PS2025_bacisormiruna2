package com.example.demo.dto.postdto;

import com.example.demo.dto.commentdto.CommentDTO;
import com.example.demo.dto.hashtagdto.HashtagDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateDTO {
    private String content;
    private String imageUrl;
    private Boolean isPublic;
    private List<CommentDTO> comments;
    private Set<HashtagDTO> hashtags;
}
