package com.example.demo.service;

import com.example.demo.dto.commentdto.CommentCreateDTO;
import com.example.demo.dto.commentdto.CommentDTO;
import com.example.demo.dto.reactiondto.ReactionCountDTO;
import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import com.example.demo.enumeration.NotificationType;
import com.example.demo.enumeration.TargetType;
import com.example.demo.errorhandler.CommentNotFoundException;
import com.example.demo.errorhandler.PostNotFoundException;
import com.example.demo.errorhandler.UnauthorizedException;
import com.example.demo.mapper.CommentMapper;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.PostRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private WebClient webClientBuilder;

    @Autowired
    private NotificationSendService notificationSendService;

    @Transactional
    public CommentDTO addComment(Long postId, CommentCreateDTO commentCreateDto, String username, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found with id: " + postId));
        if (post.getIsPublic().equals(1)) {//sa pot adauga comentarii doar la postarile publice (mai tarziu implementez si sa fie a prietenilor mei)
            throw new IllegalStateException("You can only comment on public posts.");
        }
        Comment comment = commentMapper.fromCreateDto(commentCreateDto);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        comment.setPost(post);
        comment.setUsername(username);
        comment.setAuthorId(userId);

        if (commentCreateDto.getImageUrl() != null ) {
            comment.setImageUrl(commentCreateDto.getImageUrl());
        }
        Comment savedComment = commentRepository.save(comment);
        return commentMapper.toDto(savedComment);
    }

    @Transactional
    public CommentDTO updateComment(Long commentId, CommentCreateDTO commentCreateDto, String username) {
        Comment existingComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found with id: " + commentId));

        if (!existingComment.getUsername().equals(username)) {
            throw new UnauthorizedException("You can only update your own comments");
        }

        existingComment.setContent(commentCreateDto.getContent());
        existingComment.setUpdatedAt(LocalDateTime.now());

        if (commentCreateDto.getImageUrl() != null) {
            existingComment.setImageUrl(commentCreateDto.getImageUrl());
        } else {
            existingComment.setImageUrl(null);
        }

        Comment updatedComment = commentRepository.save(existingComment);
        return commentMapper.toDto(updatedComment);
    }

    @Transactional
    public void deleteComment(Long commentId, String username) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found with id: " + commentId));

        System.out.println("Comment posted by: " + comment.getUsername());
        System.out.println("Logged in user: " + username);  // Adaugă un log pentru a verifica
        if (!comment.getUsername().equals(username)) {
            throw new UnauthorizedException("You can only delete your own comments");
        }
        commentRepository.delete(comment);
    }

    @Transactional
    public void deleteCommentAsModerator(Long commentId, String username) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found with id: " + commentId));
        boolean isModeratorAction = !comment.getUsername().equals(username) && username.equals("Moderator");
        Long authorId = comment.getAuthorId(); // Presupunem că există un authorId în Comment
        commentRepository.delete(comment);

        if (isModeratorAction) {
            notificationSendService.sendNotification(
                    authorId,
                    "Your comment has been deleted by a moderator.",
                    NotificationType.COMMENT_DELETED
            );
        }
    }


    public List<ReactionCountDTO> getReactionsForTarget(Long targetId, TargetType targetType) {
        return webClientBuilder.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/reactions/reactions")
                        .queryParam("targetId", targetId)
                        .queryParam("targetType", targetType)
                        .build())
                .retrieve()
                .bodyToFlux(ReactionCountDTO.class)
                .collectList()
                .block();
    }

    public CommentDTO toDtoWithReactions(Comment comment) {
        CommentDTO dto = commentMapper.toDto(comment);
        List<ReactionCountDTO> reactions = getReactionsForTarget(comment.getId(), TargetType.COMMENT);
        dto.setReactions(reactions);
        return dto;
    }

    public CommentDTO getCommentById(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found with id: " + id));
        return commentMapper.toDto(comment);
    }

    public boolean existsById(Long id) {
        return commentRepository.existsById(id);
    }
}