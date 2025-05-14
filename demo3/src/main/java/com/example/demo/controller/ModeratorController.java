package com.example.demo.controller;
import com.example.demo.dto.activitylogdto.ActivityLogDTO;
import com.example.demo.errorhandler.UserAlreadyBlockedException;
import com.example.demo.errorhandler.UserNotBlockedException;
import com.example.demo.service.JWTService;
import com.example.demo.service.ModeratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/validator")
public class ModeratorController {
    @Autowired
    private JWTService jwtService;
    @Autowired
    private ModeratorService moderatorService;

    @GetMapping("/isModerator")
    public ResponseEntity<Boolean> isModerator(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
        }
        String token = authHeader.substring(7);
        try {
            String role = jwtService.extractRoleName(token); // extragi userRole din token
            return ResponseEntity.ok(role.equals("moderator"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
        }
    }

    private Mono<Boolean> validateModerator(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.just(false);
        }

        String token = authHeader.substring(7);
        try {
            String role = jwtService.extractRoleName(token);
            return Mono.just(role.equals("moderator"));
        } catch (Exception e) {
            return Mono.just(false);
        }
    }

    @PutMapping("/users/{userId}/block")
    public ResponseEntity<String> blockUser(
            @PathVariable Long userId,
            @RequestBody String reason,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token.");
        }
        String token = authHeader.substring(7);
        try {
            boolean success = moderatorService.blockUser(userId, reason, token);
            return ResponseEntity.ok("User successfully blocked");
        } catch (UserAlreadyBlockedException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error blocking user: " + e.getMessage());
        }
    }

    @PutMapping("/users/{userId}/unblock")
    public ResponseEntity<String> unblockUser(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token.");
        }
        String token = authHeader.substring(7);
        try {
            boolean success = moderatorService.unblockUser(userId, token);
            return ResponseEntity.ok("User successfully unblocked");
        } catch (UserNotBlockedException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error unblocking user: " + e.getMessage());
        }
    }

    @GetMapping("/users/{userId}/isBlocked")
    public ResponseEntity<Boolean> isUserBlocked(@PathVariable Long userId) {
        try {
            boolean isBlocked = moderatorService.isUserBlocked(userId);
            return ResponseEntity.ok(isBlocked);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    @PostMapping("/delete-post")
    public Mono<ResponseEntity<String>> deletePost(@RequestParam Long postId,
                                                   @RequestParam Long authorId,
                                                   @RequestParam String reason,
                                                   @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token."));
        }

        String token = authHeader.substring(7);
        String role = jwtService.extractRoleName(token);
        if (!role.equalsIgnoreCase("moderator")) {
            return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only moderators can delete posts."));
        }

        return moderatorService.deletePostAsModerator(postId, authorId, reason, token)
                .map(success -> ResponseEntity.ok("Post marked as deleted."))
                .onErrorResume(IllegalStateException.class, e -> Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage())))
                .onErrorResume(IllegalArgumentException.class, e -> {
                    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error deleting post: " + e.getMessage()));
                })
                .onErrorResume(Exception.class, e -> {
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting post: " + e.getMessage()));
                });
    }


    @PostMapping("/delete-comment")
    public Mono<ResponseEntity<String>> deleteComment(@RequestParam Long commentId,
                                                      @RequestParam Long authorId,
                                                      @RequestParam String reason,
                                                      @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token."));
        }

        String token = authHeader.substring(7);
        String role = jwtService.extractRoleName(token);
        if (!role.equalsIgnoreCase("moderator")) {
            return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only moderators can delete comments."));
        }
        return moderatorService.deleteCommentAsModerator(commentId, authorId, reason, token)
                .map(success -> ResponseEntity.ok("Comment marked as deleted."))
                .onErrorResume(IllegalArgumentException.class, e ->
                        Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()))) // Handle comment not found
                .onErrorResume(IllegalStateException.class, e ->
                        Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()))) // Handle already deleted comment
                .onErrorResume(Exception.class, e -> {
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting comment: " + e.getMessage()));
                });
    }

//    @GetMapping("/notifications/{userId}")
//    public ResponseEntity<List<ActivityLogDTO>> getActivities(@PathVariable Long userId) {
//        List<ActivityLogDTO> activities =moderatorService.getActivitiesByUserId(userId);
//        return ResponseEntity.ok(activities);
//    }

    @GetMapping("/activity-log")
    public ResponseEntity<?> getActivityLog(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token.");
        }
        String token = authHeader.substring(7);
        try {
            List<ActivityLogDTO> activities = moderatorService.getActivitiesByUserId(token);
            return ResponseEntity.ok(activities);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving activity log: " + e.getMessage());
        }
    }







}
