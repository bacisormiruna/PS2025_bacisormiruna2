package com.example.demo.controller;

import com.example.demo.dto.postdto.PostDTO;
import com.example.demo.entity.User;
import com.example.demo.errorhandler.CommentNotFoundException;
import com.example.demo.errorhandler.PostNotFoundException;
import com.example.demo.errorhandler.UnauthorizedException;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AdminService;
import com.example.demo.service.JWTService;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping(value = "/api/moderator")
@RequiredArgsConstructor
public class ModeratorController {

    @Autowired
    private AdminService adminService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private final JWTService jwtService;

    @PutMapping("/blockUser/{userId}")
    public ResponseEntity<String> blockUser(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            List<User> admins = userRepository.findUserByRoleName("ADMIN");
            if (admins.contains(user)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin users cannot be blocked");
            }
            adminService.blockUser(userId);
            return ResponseEntity.ok("User blocked successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    @PutMapping("/unblockUser/{userId}")
    public ResponseEntity<String> unblockUser(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            List<User> admins = userRepository.findUserByRoleName("ADMIN");
            if (admins.contains(user)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin users cannot be unblocked");
            }
            adminService.unblockUser(userId);
            return ResponseEntity.ok("User unblocked successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    @DeleteMapping("/deletePost/{postId}")
    public ResponseEntity<?> deletePost(
            @PathVariable Long postId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String username = jwtService.extractUsername(token);
            Long userId = jwtService.extractUserId(token);
            userService.deletePost(postId, username, userId, authHeader);
            return ResponseEntity.noContent().build();
        } catch (PostNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error when deleting post: " + e.getMessage());
        }
    }


    @DeleteMapping("/deleteComment/{commentId}")
    public ResponseEntity<?> deleteUserComment(
            @PathVariable Long commentId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String username = jwtService.extractUsername(token);
            Long userId = jwtService.extractUserId(token);
            userService.deleteCommentFromPost(commentId, username, userId, authHeader);
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
