package com.example.demo.mapper;

import com.example.demo.dto.hashtagdto.HashtagDTO;
import com.example.demo.entity.Hashtag;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class HashtagMapper {
    public HashtagDTO toDto(Hashtag hashtag) {
        if (hashtag == null) return null;

        return new HashtagDTO(
                hashtag.getId(),
                hashtag.getName()
        );
    }

    public Hashtag toEntity(HashtagDTO dto) {
        if (dto == null) return null;

        Hashtag hashtag = new Hashtag();
        hashtag.setId(dto.getId());
        hashtag.setName(dto.getName());
        return hashtag;
    }
    public List<HashtagDTO> toDtoList(Collection<Hashtag> hashtags) {
        return hashtags.stream().map(this::toDto).collect(Collectors.toList());
    }

    public Set<Hashtag> toEntitySet(Collection<HashtagDTO> dtos) {
        return dtos.stream().map(this::toEntity).collect(Collectors.toSet());
    }
}
