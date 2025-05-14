package com.example.demo.repository;

import com.example.demo.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    Optional<ActivityLog> findByUserIdAndIsBlockedTrue(Long userId);
    Optional<ActivityLog> findByPostId(Long postId);
    Optional<ActivityLog> findByCommentId(Long postId);
    List<ActivityLog> findByUserId(Long userId);
    boolean existsByPostId(Long postId);
    boolean existsByCommentId(Long commentId);
}
