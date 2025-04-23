package com.example.demo.mapper;

import com.example.demo.dto.commentdto.CommentCreateDTO;
import com.example.demo.dto.commentdto.CommentDTO;
import com.example.demo.dto.reactiondto.ReactionCountDTO;
import com.example.demo.entity.Comment;
import com.example.demo.enumeration.TargetType;
import com.example.demo.service.PostService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommentMapper {
    private final PostService postService;

    public CommentMapper(PostService postService) {
        this.postService = postService;
    }

    public CommentDTO toDto(Comment comment) {
        if (comment == null) {
            return null;
        }
        List<ReactionCountDTO> reactions = postService.getReactionsForTarget(comment.getId(), TargetType.COMMENT);
        CommentDTO commentDto = new CommentDTO();
        commentDto.setId(comment.getId());
        commentDto.setContent(comment.getContent());
        commentDto.setImageUrl(comment.getImageUrl());
        commentDto.setCreatedAt(comment.getCreatedAt());
        commentDto.setUpdatedAt(comment.getUpdatedAt());
        commentDto.setUsername(comment.getUsername());
        commentDto.setAuthorId(comment.getAuthorId());
        commentDto.setPostId(comment.getPost() != null ? comment.getPost().getId() : null);
        commentDto.setReactions(reactions);
        return commentDto;
    }

    public Comment toEntity(CommentDTO commentDto) {
        if (commentDto == null) {
            return null;
        }
        Comment comment = new Comment();
        comment.setId(commentDto.getId());
        comment.setContent(commentDto.getContent());
        comment.setImageUrl(commentDto.getImageUrl());
        comment.setCreatedAt(commentDto.getCreatedAt());
        comment.setUpdatedAt(commentDto.getUpdatedAt());
        comment.setUsername(commentDto.getUsername());
        comment.setAuthorId(commentDto.getAuthorId());

        return comment;
    }

    public Comment fromCreateDto(CommentCreateDTO commentCreateDto) {
        if (commentCreateDto == null) {
            return null;
        }
        Comment comment = new Comment();
        comment.setContent(commentCreateDto.getContent());
        return comment;
    }
}
