package com.example.demo.service;

import com.example.demo.dto.hashtagdto.HashtagDTO;
import com.example.demo.entity.Hashtag;
import com.example.demo.repository.HashtagRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HashtagService {
    private final HashtagRepository hashtagRepository;

    @Autowired
    public HashtagService(HashtagRepository hashtagRepository) {
        this.hashtagRepository = hashtagRepository;
    }

    public List<Hashtag> getAllHashtags() {
        return hashtagRepository.findAll();
    }

    public HashtagDTO createHashtag(HashtagDTO hashtagDto) {
        Hashtag hashtag = new Hashtag();
        hashtag.setName(hashtagDto.getName());
        Hashtag savedHashtag = hashtagRepository.save(hashtag);
            return new HashtagDTO(savedHashtag.getId(),savedHashtag.getName());
    }

    @Transactional
    public Hashtag findOrCreateHashtag(String name) {
        String normalizedName = name.startsWith("#") ? name : "#" + name;

        return hashtagRepository.findByName(normalizedName)
                .orElseGet(() -> {
                    Hashtag newHashtag = new Hashtag(normalizedName);
                    return hashtagRepository.save(newHashtag);
                });
    }

//    @Transactional
//    public Hashtag findOrCreateHashtag(String name) {
//        // Ensure name is not null or empty
//        if (name == null || name.trim().isEmpty()) {
//            throw new IllegalArgumentException("Hashtag name cannot be null or empty");
//        }
//
//        // Normalize hashtag name - ensure it has a # prefix
//        String normalizedName = name.startsWith("#") ? name : "#" + name;
//
//        System.out.println("Searching for hashtag: " + normalizedName);
//
//        // Try to find existing hashtag
//        Optional<Hashtag> existingHashtag = hashtagRepository.findByName(normalizedName);
//
//        if (existingHashtag.isPresent()) {
//            Hashtag hashtag = existingHashtag.get();
//            System.out.println("Found existing hashtag: ID=" + hashtag.getId() + ", Name=" + hashtag.getName());
//            return hashtag;
//        } else {
//            System.out.println("Creating new hashtag: " + normalizedName);
//
//            Hashtag newHashtag = new Hashtag();
//            newHashtag.setName(normalizedName);
//
//            // Save and flush to ensure ID is generated immediately
//            Hashtag savedHashtag = hashtagRepository.saveAndFlush(newHashtag);
//
//            System.out.println("Created new hashtag: ID=" + savedHashtag.getId() + ", Name=" + savedHashtag.getName());
//            return savedHashtag;
//        }
//    }

    @Transactional
    public void deleteHashtag(String name) {
        hashtagRepository.deleteByNameIgnoreCase(name);
    }
}
