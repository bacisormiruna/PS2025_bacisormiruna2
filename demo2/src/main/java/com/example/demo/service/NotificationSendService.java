package com.example.demo.service;

import com.example.demo.dto.notificationdto.NotificationDTO;
import com.example.demo.enumeration.NotificationType;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class NotificationSendService {
    private final WebClient notificationWebClient;

    public NotificationSendService(WebClient notificationWebClient) {
        this.notificationWebClient = notificationWebClient;
    }

    public void sendNotification(Long userId, String message, NotificationType notificationType) {
        NotificationDTO request = new NotificationDTO(userId, message, notificationType);

        notificationWebClient.post()
                .uri("/api/notifications/create")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> Mono.empty())
                .subscribe();
    }
}
