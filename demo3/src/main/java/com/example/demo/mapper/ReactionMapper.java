package com.example.demo.mapper;

import com.example.demo.dto.reactiondto.ReactionCreateDTO;
import com.example.demo.dto.reactiondto.ReactionDTO;
import com.example.demo.entity.Reaction;

import java.time.LocalDateTime;

public class ReactionMapper {

    public static Reaction toEntity(ReactionCreateDTO dto) {
        if (dto == null) {
            return null;
        }

        return Reaction.builder()
                .userId(dto.getUserId())
                .targetId(dto.getTargetId())
                .targetType(dto.getTargetType())
                .type(dto.getType())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static ReactionDTO toDto(Reaction entity) {
        if (entity == null) {
            return null;
        }
        ReactionDTO dto = new ReactionDTO();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setTargetId(entity.getTargetId());
        dto.setTargetType(entity.getTargetType());
        dto.setType(entity.getType());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    public static void updateReactionFromDto(Reaction reaction, ReactionCreateDTO dto) {
        if (reaction != null && dto != null) {
            reaction.setType(dto.getType());
            reaction.setUpdatedAt(LocalDateTime.now());
        }
    }
}
