package com.example.demo.controller;

import com.example.demo.dto.commentdto.CommentCreateDTO;
import com.example.demo.dto.commentdto.CommentDTO;
import com.example.demo.dto.reactiondto.ReactionCreateDTO;
import com.example.demo.enumeration.TargetType;
import com.example.demo.errorhandler.PostNotFoundException;
import com.example.demo.service.CommentService;
import com.example.demo.service.JWTService;
import com.example.demo.service.PostService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;
    @Autowired
    private JWTService jwtService;
    @Autowired
    private PostService postService;

    @PostMapping(
            value = "/addComment/{postId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> addComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateDTO commentCreateDto,
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.substring(7);

        try {
            String username = jwtService.extractUsername(token);
            Long userId = jwtService.extractUserId(token);

            CommentDTO createdComment = commentService.addComment(postId, commentCreateDto, username, userId);
            return ResponseEntity.ok(createdComment);

        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token invalid");
        } catch (PostNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/updateComment/{commentId}")
    public ResponseEntity<CommentDTO> updateComment(HttpServletRequest request, @PathVariable Long commentId, @RequestBody CommentCreateDTO commentDto) {
        String username = (String) request.getAttribute("username");
        return ResponseEntity.ok(commentService.updateComment(commentId, commentDto, username));
    }

    @DeleteMapping("/deleteComment/{commentId}")
    public ResponseEntity<Void> deleteComment(HttpServletRequest request, @PathVariable Long commentId) {
        String username = (String) request.getAttribute("username");
        commentService.deleteComment(commentId, username);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{commentId}/react")
    public ResponseEntity<Void> reactToPost(@PathVariable Long commentId,
                                            @RequestBody ReactionCreateDTO dto) {
        dto.setTargetId(commentId);
        dto.setTargetType(TargetType.COMMENT);
        postService.sendReaction(dto);
        return ResponseEntity.ok().build();
    }
}