package com.example.demo.controller;

import com.example.demo.dto.postdto.PostDTO;
import com.example.demo.entity.Post;
import com.example.demo.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

// PostController.java
@RestController
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Post> createPost(
            @RequestParam Long authorId,
            @RequestParam String content,
            @RequestParam(required = false) MultipartFile image,
            @RequestParam Set<String> hashtags
    ) {
        String imageUrl = postService.storeImage(image); // Metodă nouă care salvează imaginea și returnează URL-ul
        Post post = postService.addPost(authorId, content, imageUrl, hashtags);
        return ResponseEntity.ok(post);
    }


    @PutMapping("/{postId}")
    public ResponseEntity<Post> updatePost(
            @PathVariable Long postId,
            @RequestParam Long authorId,
            @RequestParam String content,
            @RequestParam(required = false) String imageUrl,
            @RequestParam Set<String> hashtags) {
        Post updatedPost = postService.updatePost(postId, authorId, content, imageUrl, hashtags);
        return ResponseEntity.ok(updatedPost);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId, @RequestParam Long authorId) {
        postService.deletePost(postId, authorId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<Post>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Post>> searchPosts(@RequestParam String keyword) {
        return ResponseEntity.ok(postService.searchPosts(keyword));
    }

    @GetMapping("/hashtag/{hashtag}")
    public ResponseEntity<List<Post>> getPostsByHashtag(@PathVariable String hashtag) {
        return ResponseEntity.ok(postService.getPostsByHashtag(hashtag));
    }

    @GetMapping("/user/{authorId}")
    public ResponseEntity<List<Post>> getPostsByUser(@PathVariable Long authorId) {
        return ResponseEntity.ok(postService.getPostsByUser(authorId));
    }
}

