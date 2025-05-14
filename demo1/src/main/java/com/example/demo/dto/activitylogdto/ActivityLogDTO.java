package com.example.demo.dto.activitylogdto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLogDTO {
    private Long moderatorId;
    private Long userId;
    private Long postId;
    private Long commentId;
    private boolean isBlocked;
    private String reason;
    private LocalDateTime timestamp;
}
