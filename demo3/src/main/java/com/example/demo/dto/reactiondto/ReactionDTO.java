package com.example.demo.dto.reactiondto;

import com.example.demo.enumeration.ReactionType;
import com.example.demo.enumeration.TargetType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReactionDTO {
    private Long id;
    private Long userId;
    private Long targetId;
    private TargetType targetType;
    private ReactionType type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
