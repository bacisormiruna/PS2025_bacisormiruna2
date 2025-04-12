package com.example.demo.service;

import com.example.demo.dto.commentdto.CommentDTO;
import com.example.demo.dto.postdto.PostDTO;
import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import com.example.demo.errorhandler.CommentNotFoundException;
import com.example.demo.errorhandler.PostNotFoundException;
import com.example.demo.errorhandler.UnauthorizedException;
import com.example.demo.mapper.CommentMapper;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.PostRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentMapper commentMapper;

    public CommentDTO addComment(Long postId, CommentDTO commentDto) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CommentNotFoundException("Post not found with id: " + postId));

        Comment comment = commentMapper.toEntity(commentDto);
        comment.setPublishTime(LocalDateTime.now());
        comment.setPost(post);

        Comment savedComment = commentRepository.save(comment);
        return commentMapper.toDto(savedComment);
    }

    public CommentDTO updateComment(Long commentId, CommentDTO commentDto, String username) {
        Comment existingComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found with id: " + commentId));

        // Verifică dacă utilizatorul este autorul comentariului
        if (!existingComment.getUsername().equals(username)) {
            throw new UnauthorizedException("You can only update your own comments");
        }

        // Actualizează textul comentariului
        existingComment.setContent(commentDto.getContent());
        if (commentDto.getImageUrl() != null) {
            existingComment.setImageUrl(commentDto.getImageUrl());
        }

        Comment updatedComment = commentRepository.save(existingComment);
        return commentMapper.toDto(updatedComment);
    }

    public void deleteComment(Long commentId, String username) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found with id: " + commentId));

        // Verifică dacă utilizatorul este autorul comentariului
        if (!comment.getUsername().equals(username)) {
            throw new UnauthorizedException("You can only delete your own comments");
        }

        commentRepository.delete(comment);
    }
}