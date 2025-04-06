package com.example.demo.dto.hashtagdto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HashtagDTO {
    private Long id;
    private String name;
    private Long postId;

    public HashtagDTO(Long id, String name) {
        this.id=id;
        this.name=name;
    }
}
