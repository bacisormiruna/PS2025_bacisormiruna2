package com.example.demo.dto.reactiondto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TotalReactionsDTO {
    private Long postReactions;
    private Long commentReactions;
    private Long totalReactions;
}
