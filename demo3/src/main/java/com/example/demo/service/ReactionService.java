package com.example.demo.service;

import com.example.demo.dto.reactiondto.ReactionCountDTO;
import com.example.demo.dto.reactiondto.ReactionCreateDTO;
import com.example.demo.entity.Reaction;
import com.example.demo.enumeration.ReactionType;
import com.example.demo.enumeration.TargetType;
import com.example.demo.mapper.ReactionMapper;
import com.example.demo.repository.ReactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReactionService {

    private final ReactionRepository reactionRepository;

    public void reactToTarget(ReactionCreateDTO dto) {
        Reaction existingReaction = reactionRepository
                .findByUserIdAndTargetIdAndTargetType(dto.getUserId(), dto.getTargetId(), dto.getTargetType())
                .orElse(null);

        if (existingReaction == null) {
            Reaction newReaction = ReactionMapper.toEntity(dto);
            reactionRepository.save(newReaction);
        } else {
            ReactionMapper.updateReactionFromDto(existingReaction, dto);
            reactionRepository.save(existingReaction);
        }
    }

    public List<ReactionCountDTO> getReactionsForTarget(Long targetId, TargetType targetType) {
        List<Object[]> results = reactionRepository.countReactionsByType(targetId, targetType);
        return results.stream()
                .map(obj -> new ReactionCountDTO((ReactionType) obj[0], (Long) obj[1]))
                .toList();
    }
}

