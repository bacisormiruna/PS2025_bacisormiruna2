package com.example.demo.mapper;

import com.example.demo.dto.hashtagdto.HashtagDTO;
import com.example.demo.dto.postdto.PostCreateDTO;
import com.example.demo.dto.postdto.PostDTO;
import com.example.demo.entity.Hashtag;
import com.example.demo.entity.Post;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.stream.Collectors;

import static java.lang.System.currentTimeMillis;

@Component
public class PostMapper {

    public PostDTO toDto(Post post) {
        if (post == null) {
            return null;
        }
        PostDTO postDto = new PostDTO();
        postDto.setId(post.getId());
        postDto.setAuthorId(post.getAuthorId());
        postDto.setUsername(post.getUsername());
        postDto.setContent(post.getContent());
        postDto.setImageUrl(post.getImageUrl());
        postDto.setIsPublic(post.getIsPublic());
        postDto.setCreatedAt(post.getCreatedAt());
        postDto.setUpdatedAt(post.getUpdatedAt());

        if (post.getHashtags() != null) {
            postDto.setHashtags(post.getHashtags().stream()
                    .map(hashtag -> new HashtagDTO(hashtag.getId(), hashtag.getName()))
                    .collect(Collectors.toCollection(HashSet::new)));
        }
        return postDto;
    }

    public Post toEntity(PostDTO postDto) {
        if (postDto == null) {
            return null;
        }

        Post post = new Post();
        post.setAuthorId(postDto.getAuthorId());
        post.setContent(postDto.getContent());
        post.setImageUrl(postDto.getImageUrl());
        post.setUsername(postDto.getUsername()); // Asigură-te că ai setat username-ul corect
        post.setIsPublic(postDto.getIsPublic());
        post.setCreatedAt(postDto.getCreatedAt());
        post.setUpdatedAt(postDto.getUpdatedAt());

        // Dacă hashtags este null, îl setăm ca o colecție goală
        if (postDto.getHashtags() != null) {
            post.setHashtags(postDto.getHashtags().stream()
                    .map(hashtagDto -> new Hashtag(hashtagDto.getName()))
                    .collect(Collectors.toSet()));
        } else {
            post.setHashtags(new HashSet<>());
        }
        return post;
    }

    public Post toEntity(PostCreateDTO postCreateDTO, Long authorId, String username) {
        if (postCreateDTO == null) {
            return null;
        }

        Post post = new Post();
        post.setContent(postCreateDTO.getContent());
        post.setImageUrl(postCreateDTO.getImageUrl());
        post.setIsPublic(postCreateDTO.getIsPublic());
        post.setAuthorId(authorId);
        post.setUsername(username);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());

        if (postCreateDTO.getHashtags() != null) {
            post.setHashtags(postCreateDTO.getHashtags().stream()
                    .map(hashtagDto -> new Hashtag(hashtagDto.getName()))
                    .collect(Collectors.toSet()));
        } else {
            post.setHashtags(new HashSet<>());
        }

        return post;
    }


}