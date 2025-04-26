package com.example.demo.mapper;

import com.example.demo.dto.notificationdto.NotificationDTO;
import com.example.demo.entity.Notification;

import java.time.LocalDateTime;

public class NotificationMapper {
    public static Notification toEntity(NotificationDTO dto) {
        if (dto == null) {
            return null;
        }

        return Notification.builder()
                .userId(dto.getUserId())
                .message(dto.getMessage())
                .notificationType(dto.getNotificationType())
                .timestamp(LocalDateTime.now())
                .read(false)
                .build();
    }

    public static NotificationDTO toDto(Notification notification) {
        if (notification == null) {
            return null;
        }
        return new NotificationDTO(
                notification.getUserId(),
                notification.getMessage(),
                notification.getNotificationType()
        );
    }
}
