package com.example.demo.dto.hashtagdto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HashtagDTO {
    private Long id;
    private String name;

    public HashtagDTO(String name) {
        this.name = name;
    }
}
