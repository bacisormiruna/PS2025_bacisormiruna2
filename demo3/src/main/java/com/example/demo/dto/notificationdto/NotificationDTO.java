package com.example.demo.dto.notificationdto;

import com.example.demo.enumeration.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long userId;
    private String message;
    private NotificationType notificationType;
}
