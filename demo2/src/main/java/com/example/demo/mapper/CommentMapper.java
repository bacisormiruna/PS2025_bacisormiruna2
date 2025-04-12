package com.example.demo.mapper;

import com.example.demo.dto.commentdto.CommentDTO;
import com.example.demo.entity.Comment;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    public CommentDTO toDto(Comment comment) {
        if (comment == null) {
            return null;
        }
        CommentDTO commentDto = new CommentDTO();
        commentDto.setId(comment.getId());
        commentDto.setContent(comment.getContent());
        commentDto.setUsername(comment.getUsername());
        commentDto.setPostId(comment.getPost().getId()); // Presupunem că un comentariu este asociat unui post
        commentDto.setCreatedAt(comment.getPublishTime());

        return commentDto;
    }

    public Comment toEntity(CommentDTO commentDto) {
        if (commentDto == null) {
            return null;
        }

        Comment comment = new Comment();
        comment.setId(commentDto.getId());
        comment.setContent(commentDto.getContent());
        comment.setUsername(commentDto.getUsername());
        comment.setPublishTime(commentDto.getCreatedAt());

        return comment;
    }
}
