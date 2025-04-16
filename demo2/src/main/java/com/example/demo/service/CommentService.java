package com.example.demo.service;

import com.example.demo.dto.commentdto.CommentCreateDTO;
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

    @Transactional
    public CommentDTO addComment(Long postId, CommentCreateDTO commentCreateDto, String username, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found with id: " + postId));
        Comment comment = commentMapper.fromCreateDto(commentCreateDto);

        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        comment.setPost(post);
        comment.setUsername(username);
        comment.setAuthorId(userId);

        if (commentCreateDto.getImageURL() != null ) {
            comment.setImageUrl(commentCreateDto.getImageURL());
        }
        Comment savedComment = commentRepository.save(comment);
        return commentMapper.toDto(savedComment);
    }

    public CommentDTO updateComment(Long commentId, CommentCreateDTO commentCreateDto, String username) {
        Comment existingComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found with id: " + commentId));

        if (!existingComment.getUsername().equals(username)) {
            throw new UnauthorizedException("You can only update your own comments");
        }

        existingComment.setContent(commentCreateDto.getContent());
        if (commentCreateDto.getImageURL() != null ) {
            existingComment.setImageUrl(commentCreateDto.getImageURL());
        }

        Comment updatedComment = commentRepository.save(existingComment);
        return commentMapper.toDto(updatedComment);
    }

    public void deleteComment(Long commentId, String username) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found with id: " + commentId));
        if (!comment.getUsername().equals(username)) {
            throw new UnauthorizedException("You can only delete your own comments");
        }

        commentRepository.delete(comment);
    }
}