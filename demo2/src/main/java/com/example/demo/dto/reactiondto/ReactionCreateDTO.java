package com.example.demo.dto.reactiondto;

import com.example.demo.enumeration.ReactionType;
import com.example.demo.enumeration.TargetType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReactionCreateDTO {
    private Long targetId;
    private TargetType targetType;
    private ReactionType type;
    private Long userId;
}
