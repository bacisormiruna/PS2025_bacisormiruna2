package com.example.demo.controller;

import com.example.demo.dto.commentdto.CommentCreateDTO;
import com.example.demo.dto.commentdto.CommentDTO;
import com.example.demo.errorhandler.CommentNotFoundException;
import com.example.demo.errorhandler.PostNotFoundException;
import com.example.demo.errorhandler.UnauthorizedException;
import com.example.demo.service.CommentService;
import com.example.demo.service.JWTService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;
    @Autowired
    private JWTService jwtService;

    @GetMapping("/{id}")
    public ResponseEntity<CommentDTO> getCommentById(@PathVariable Long id) {
        return ResponseEntity.ok(commentService.getCommentById(id));
    }

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

//    @DeleteMapping("/deleteComment/{commentId}")
//    public ResponseEntity<Void> deleteComment(HttpServletRequest request, @PathVariable Long commentId) {
//        String username = (String) request.getAttribute("username");
//        commentService.deleteComment(commentId, username);
//        return ResponseEntity.noContent().build();
//    }

//    @DeleteMapping("/deleteComment/{commentId}")
//    public ResponseEntity<?> deleteComment(
//            @PathVariable Long commentId,
//            @RequestHeader("Authorization") String authHeader) {
//        try {
//            String token = authHeader.substring(7);
//            String username = jwtService.extractUsername(token);
//            commentService.deleteComment(commentId, username);
//            return ResponseEntity.noContent().build();
//        } catch (CommentNotFoundException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Comment not found");
//        } catch (UnauthorizedException e) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Error when deleting comment: " + e.getMessage());
//        }
//    }

    @DeleteMapping("/deleteComment/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long commentId,
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(value = "moderatorAction", required = false) Boolean moderatorAction) {
        try {
            String token = authHeader.substring(7);
            String username = jwtService.extractUsername(token);

            if (Boolean.TRUE.equals(moderatorAction)) {
                commentService.deleteCommentAsModerator(commentId,username);
            } else {
                commentService.deleteComment(commentId, username);
            }
            return ResponseEntity.noContent().build();
        } catch (CommentNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Comment not found");
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error when deleting comment: " + e.getMessage());
        }
    }

}