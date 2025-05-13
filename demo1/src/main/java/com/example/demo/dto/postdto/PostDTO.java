package com.example.demo.dto.postdto;


import com.example.demo.dto.commentdto.CommentDTO;
import com.example.demo.dto.hashtagdto.HashtagDTO;
import com.example.demo.dto.reactiondto.ReactionCountDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDTO {
    private Long id;
    private Long authorId;
    private String username;
    private String content;
    private String imageUrl;
    private Boolean isPublic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CommentDTO> comments;
    private Set<HashtagDTO> hashtags;
    private List<ReactionCountDTO> reactions;
    private Long totalReactions;
    private long totalReactionsForComments;
}