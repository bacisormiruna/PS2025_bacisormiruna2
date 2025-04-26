package com.example.demo.controller;

import com.example.demo.dto.notificationdto.NotificationDTO;
import com.example.demo.entity.Notification;
import com.example.demo.service.JWTService;
import com.example.demo.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private JWTService jwtService;

    @GetMapping
    public ResponseEntity<?> getUserNotifications(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtService.extractUserId(token);

            List<Notification> notifications = notificationService.getUserNotifications(userId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching notifications: " + e.getMessage());
        }
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable Long notificationId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            notificationService.markNotificationAsRead(notificationId);
            return ResponseEntity.ok("Notification marked as read");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error marking notification as read: " + e.getMessage());
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createNotification(@RequestBody NotificationDTO request) {
        try {
            notificationService.createNotification(
                    request.getUserId(),
                    request.getMessage(),
                    request.getNotificationType()
            );
            return ResponseEntity.ok("Notification created successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating notification: " + e.getMessage());
        }
    }
}

