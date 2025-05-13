package com.example.demo.mapper;

import com.example.demo.dto.activitylogdto.ActivityLogDTO;
import com.example.demo.entity.ActivityLog;

import java.time.LocalDateTime;

public class ActivityLogMapper {

    public static ActivityLog toEntity(ActivityLogDTO dto) {
        if (dto == null) {
            return null;
        }
        return ActivityLog.builder()
                .moderatorId(dto.getModeratorId())
                .userId(dto.getUserId())
                .postId(dto.getPostId())
                .commentId(dto.getCommentId())
                .isBlocked(dto.isBlocked())
                .reason(dto.getReason())
                .timestamp(dto.getTimestamp() != null ? dto.getTimestamp() : LocalDateTime.now())
                .build();
    }

    public static ActivityLogDTO toDto(ActivityLog entity) {
        if (entity == null) {
            return null;
        }

        return ActivityLogDTO.builder()
                .moderatorId(entity.getModeratorId())
                .userId(entity.getUserId())
                .postId(entity.getPostId())
                .commentId(entity.getCommentId())
                .isBlocked(entity.isBlocked())
                .reason(entity.getReason())
                .timestamp(entity.getTimestamp())
                .build();
    }
}
