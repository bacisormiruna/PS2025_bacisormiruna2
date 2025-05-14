package com.example.demo.service;
import com.example.demo.dto.activitylogdto.ActivityLogDTO;
import com.example.demo.entity.ActivityLog;
import com.example.demo.errorhandler.UserAlreadyBlockedException;
import com.example.demo.errorhandler.UserNotBlockedException;
import com.example.demo.repository.ActivityLogRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class ModeratorService {

   private final ActivityLogRepository activityLogRepository;

    private final JWTService jwtService;

    private final WebClient webClient;
    private final String postServiceUrl = "http://localhost:8081"; // URL-ul microserviciului 2

    public ModeratorService(ActivityLogRepository activityLogRepository,JWTService jwtService,WebClient.Builder webClientBuilder) {
        this.activityLogRepository=activityLogRepository;
        this.jwtService=jwtService;
        this.webClient = webClientBuilder.baseUrl(postServiceUrl).build();
    }

    @Transactional
    public boolean blockUser(Long userId, String reason, String token) {
        Optional<ActivityLog> existingLog = activityLogRepository.findByUserIdAndIsBlockedTrue(userId);
        if (existingLog.isPresent()) {
            throw new UserAlreadyBlockedException("User with ID " + userId + " is already blocked.");
        }
        ActivityLog log = ActivityLog.builder()
                .userId(userId)
                .moderatorId(jwtService.extractUserId(token))
                .reason(reason)
                .isBlocked(true)
                .timestamp(LocalDateTime.now())
                .build();
        activityLogRepository.save(log);
        return true;
    }


    @Transactional
    public boolean unblockUser(Long userId, String token) {
        Optional<ActivityLog> existingLog = activityLogRepository.findByUserIdAndIsBlockedTrue(userId);
        if (existingLog.isEmpty()) {
            throw new UserNotBlockedException("User with ID " + userId + " is not blocked.");
        }
        ActivityLog log = existingLog.get();
        log.setBlocked(false);
        log.setTimestamp(LocalDateTime.now());
        activityLogRepository.save(log);
        return true;
    }

    public boolean isUserBlocked(Long userId) {
        Optional<ActivityLog> existingLog = activityLogRepository.findByUserIdAndIsBlockedTrue(userId);
        return existingLog.isPresent();
    }

    @Transactional
    public Mono<Boolean> deletePostAsModerator(Long postId, Long authorId, String reason, String token) {
        return webClient.get()
                .uri("/api/posts/posts/exists/{id}", postId)  // Microserviciul 2, endpoint-ul care verifică dacă postarea există
                .retrieve()
                .bodyToMono(Boolean.class)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new IllegalArgumentException("Post with ID " + postId + " does not exist."));
                    }
                    Optional<ActivityLog> existing = activityLogRepository.findByPostId(postId);
                    if (existing.isPresent()) {
                        return Mono.error(new IllegalStateException("Post already marked as deleted by a moderator."));
                    }
                    ActivityLog log = ActivityLog.builder()
                            .postId(postId)
                            .userId(authorId)
                            .isBlocked(true) //am pus pe 0 sa fie neblocat si pe false sa fie blocat
                            .moderatorId(jwtService.extractUserId(token))
                            .reason(reason)
                            .timestamp(LocalDateTime.now())
                            .build();

                    activityLogRepository.save(log);
                    return Mono.just(true);
                });
    }


    @Transactional
    public Mono<Boolean> deleteCommentAsModerator(Long commentId, Long authorId, String reason, String token) {
        return webClient.get()
                .uri("/api/comments/comments/exists/{id}", commentId)  // Microserviciul 2, endpoint-ul care verifică dacă comentariul există
                .retrieve()
                .bodyToMono(Boolean.class)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new IllegalArgumentException("Comment with ID " + commentId + " does not exist."));
                    }
                    Optional<ActivityLog> existing = activityLogRepository.findByCommentId(commentId);
                    if (existing.isPresent()) {
                        return Mono.error(new IllegalStateException("Comment already marked as deleted by a moderator."));
                    }
                    ActivityLog log = ActivityLog.builder()
                            .commentId(commentId)
                            .userId(authorId)
                            .isBlocked(true)//am pus pe 0 sa fie deblocat si pe 1 sa fie blocat adica true=0 adica neblocat si false=1 care inseamna blocat
                            .moderatorId(jwtService.extractUserId(token))
                            .reason(reason)
                            .timestamp(LocalDateTime.now())
                            .build();
                    activityLogRepository.save(log);
                    return Mono.just(true);
                });
    }

//    public List<ActivityLogDTO> getActivitiesByUserId(Long userId) {
//        return activityLogRepository.findByUserId(userId).stream()
//                .map(activity -> ActivityLogDTO.builder()
//                        .moderatorId(activity.getModeratorId())
//                        .userId(activity.getUserId())
//                        .postId(activity.getPostId())
//                        .commentId(activity.getCommentId())
//                        .isBlocked(activity.isBlocked())
//                        .reason(activity.getReason())
//                        .timestamp(activity.getTimestamp())
//                        .build())
//                .collect(Collectors.toList());
//    }
//

    public List<ActivityLogDTO> getActivitiesByUserId(String token) {
        String role = jwtService.extractRoleName(token);
        Long userId = jwtService.extractUserId(token);

        List<ActivityLog> activities;

        if ("moderator".equalsIgnoreCase(role)) {
            activities = activityLogRepository.findAll();
        } else {
            activities = activityLogRepository.findByUserId(userId);
        }

        return activities.stream()
                .map(activity -> ActivityLogDTO.builder()
                        .moderatorId(activity.getModeratorId())
                        .userId(activity.getUserId())
                        .postId(activity.getPostId())
                        .commentId(activity.getCommentId())
                        .isBlocked(activity.isBlocked())
                        .reason(activity.getReason())
                        .timestamp(activity.getTimestamp())
                        .build())
                .collect(Collectors.toList());
    }




}
