package com.example.demo.controller;

import com.example.demo.dto.hashtagdto.HashtagDTO;
import com.example.demo.entity.Hashtag;
import com.example.demo.service.HashtagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hashtags")
public class HashtagController {

    @Autowired
    private HashtagService hashtagService;

    @GetMapping
    public ResponseEntity<List<Hashtag>> getAllHashtags() {
        return ResponseEntity.ok(hashtagService.getAllHashtags());
    }

    @PostMapping
    public ResponseEntity<HashtagDTO> createHashtag(@RequestBody HashtagDTO hashtagDto) {
        return ResponseEntity.ok(hashtagService.createHashtag(hashtagDto));
    }
    @DeleteMapping("/{name}")
    public ResponseEntity<Void> deleteHashtag(@PathVariable String name) {
        hashtagService.deleteHashtag(name);
        return ResponseEntity.noContent().build();
    }
}
