package com.example.demo.dto.reactiondto;

import com.example.demo.enumeration.ReactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReactionCountDTO {
    private ReactionType type;
    private long count;
}