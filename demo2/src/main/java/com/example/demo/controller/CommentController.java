package com.example.demo.controller;

import com.example.demo.dto.commentdto.CommentDTO;
import com.example.demo.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentDTO> addComment(HttpServletRequest request, @PathVariable Long postId, @RequestBody CommentDTO commentDto) {
        String username = (String) request.getAttribute("username");
        commentDto.setUsername(username);
        return ResponseEntity.ok(commentService.addComment(postId, commentDto));
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<CommentDTO> updateComment(HttpServletRequest request, @PathVariable Long commentId, @RequestBody CommentDTO commentDto) {
        String username = (String) request.getAttribute("username");
        return ResponseEntity.ok(commentService.updateComment(commentId, commentDto, username));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(HttpServletRequest request, @PathVariable Long commentId) {
        String username = (String) request.getAttribute("username");
        commentService.deleteComment(commentId, username);
        return ResponseEntity.noContent().build();
    }
}