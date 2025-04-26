package com.example.demo.service;
import com.example.demo.entity.Notification;
import com.example.demo.enumeration.NotificationType;
import com.example.demo.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    public void createNotification(Long userId, String message, NotificationType notificationType) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setMessage(message);
        notification.setTimestamp(LocalDateTime.now());
        notification.setRead(false);
        notification.setNotificationType(notificationType);
        notificationRepository.save(notification);
    }

    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    public void markNotificationAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(
                    notification -> { notification.setRead(true);
                    notificationRepository.save(notification);
        });
    }
}
