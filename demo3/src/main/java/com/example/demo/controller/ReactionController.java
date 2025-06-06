package com.example.demo.controller;

import com.example.demo.dto.reactiondto.ReactionCountDTO;
import com.example.demo.dto.reactiondto.ReactionCreateDTO;
import com.example.demo.enumeration.TargetType;
import com.example.demo.service.ReactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reactions")
@RequiredArgsConstructor
public class ReactionController {

    private final ReactionService reactionService;

    @PostMapping("/react")
    public ResponseEntity<Void> reactToTarget(@RequestBody ReactionCreateDTO dto) {
        reactionService.reactToTarget(dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/reactions")
    public ResponseEntity<List<ReactionCountDTO>> getReactionsForTarget(
            @RequestParam Long targetId,
            @RequestParam TargetType targetType) {
        return ResponseEntity.ok(reactionService.getReactionsForTarget(targetId, targetType));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countReactions(
            @RequestParam Long targetId,
            @RequestParam TargetType targetType) {
        return ResponseEntity.ok(reactionService.countReactions(targetId, targetType));
    }

    @PostMapping("/count-batch")
    public ResponseEntity<Map<Long, Long>> countReactionsForMultipleTargets(
            @RequestParam List<Long> targetIds,
            @RequestParam TargetType targetType) {
        return ResponseEntity.ok(reactionService.countReactionsForMultipleTargets(targetIds, targetType));
    }

}

