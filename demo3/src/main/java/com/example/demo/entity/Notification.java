package com.example.demo.entity;

import com.example.demo.enumeration.NotificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String message;
    private LocalDateTime timestamp;
    @Column(name = "`read`")//because read is a reserved keyword in SQL
    private boolean read;
    private NotificationType notificationType;
}
