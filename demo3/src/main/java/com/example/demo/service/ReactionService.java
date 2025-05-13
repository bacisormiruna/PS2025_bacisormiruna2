package com.example.demo.service;

import com.example.demo.dto.reactiondto.ReactionCountDTO;
import com.example.demo.dto.reactiondto.ReactionCreateDTO;
import com.example.demo.entity.Reaction;
import com.example.demo.enumeration.ReactionType;
import com.example.demo.enumeration.TargetType;
import com.example.demo.errorhandler.AlreadyReactedException;
import com.example.demo.mapper.ReactionMapper;
import com.example.demo.repository.ReactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            if (existingReaction.getType().equals(dto.getType())) {
                throw new AlreadyReactedException("Ai reacționat deja cu acest tip.");
            } else {
                ReactionMapper.updateReactionFromDto(existingReaction, dto);
                reactionRepository.save(existingReaction);
            }
        }}

        public List<ReactionCountDTO> getReactionsForTarget (Long targetId, TargetType targetType){
            List<Object[]> results = reactionRepository.countReactionsByType(targetId, targetType);
            return results.stream()
                    .map(obj -> new ReactionCountDTO((ReactionType) obj[0], (Long) obj[1]))
                    .toList();
        }

        public Long countReactions (Long targetId, TargetType targetType){
            System.out.println("Counting reactions for targetId: " + targetId + ", targetType: " + targetType);
            Long count = reactionRepository.countAllReactionsByTargetIdAndType(targetId, targetType);
            System.out.println("Found " + count + " reactions");
            return count;
        }

        public Map<Long, Long> countReactionsForMultipleTargets (List < Long > targetIds, TargetType targetType){
            List<Object[]> results = reactionRepository.countReactionsByTargetIdsAndType(targetIds, targetType);
            Map<Long, Long> reactionsMap = new HashMap<>();
            for (Long targetId : targetIds) {
                reactionsMap.put(targetId, 0L);
            }
            for (Object[] result : results) {
                Long targetId = (Long) result[0];
                Long count = ((Number) result[1]).longValue();
                reactionsMap.put(targetId, count);
            }
            return reactionsMap;
        }
    }

