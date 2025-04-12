package com.example.demo.controller;

import com.example.demo.dto.hashtagdto.HashtagDTO;
import com.example.demo.dto.postdto.PostDTO;
import com.example.demo.entity.Hashtag;
import com.example.demo.errorhandler.PostNotFoundException;
import com.example.demo.errorhandler.UnauthorizedException;
import com.example.demo.service.JWTService;
import com.example.demo.service.PostService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;
    @Autowired
    private JWTService jwtService;

    @GetMapping
    public ResponseEntity<List<PostDTO>> getAllPosts(HttpServletRequest request) {
        List<PostDTO> posts = postService.getAllPosts();
        System.out.println("Posts in controller: " + posts.size()); // Log pentru a verifica lista returnată
        return ResponseEntity.ok(posts);
    }

    //asta e buna pentru varianta clasica primul request de create1
//    @PostMapping
//    public ResponseEntity<PostDTO> createPost(
//            @RequestPart("postDto") PostDTO postDto,
//            @RequestPart("image") MultipartFile imageFile) throws Exception {
//        PostDTO savedPost = postService.createPost(postDto, imageFile);
//        return ResponseEntity.ok(savedPost);
//    }

    // Endpoint pentru crearea unei postări
//    @PostMapping
//    public ResponseEntity<PostDTO> createPost(@RequestBody PostDTO postDTO,
//                                              @RequestParam(value = "image", required = false) MultipartFile imageFile) throws Exception {
//        PostDTO createdPost = postService.createPost1(1L, postDTO, imageFile); // presupunem că userId = 1L
//        return ResponseEntity.ok(createdPost);
//    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostDTO> createPost(
            @RequestPart("postDto") String postDtoJson,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) throws Exception {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            PostDTO postDTO = objectMapper.readValue(postDtoJson, PostDTO.class);

            // Extract userId from security context or from postDTO
            //Long userId = postDTO.getAuthorId() != null ?
            //        postDTO.getAuthorId() : getUserIdFromUsername(postDTO.getUsername());

            // Create the post
            PostDTO createdPost = postService.createPost1(postDTO, imageFile);
            System.out.println("Hashtags for the post: " + createdPost.getHashtags());
            return ResponseEntity.ok(createdPost);


        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error creating post: " + e.getMessage());
        }
    }


    @PutMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostDTO> updatePost(
            @PathVariable Long postId,
            @RequestPart("postDto") String postDtoJson,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) throws Exception {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            PostDTO postDTO = objectMapper.readValue(postDtoJson, PostDTO.class);
            //String username = principal.getName();
            PostDTO updatedPost = postService.updatePost(postId, postDTO, postDTO.getUsername(), imageFile);
            System.out.println("Updated post with hashtags: " + updatedPost.getHashtags());

            return ResponseEntity.ok(updatedPost);

        } catch (PostNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDTO> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            @RequestParam String username) {//aici era Principal principal

        try {
            postService.deletePost(postId, username);
            return ResponseEntity.noContent().build();

        } catch (PostNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/hashtag/{hashtagName}")
    public ResponseEntity<List<PostDTO>> getPostsByHashtag(@PathVariable String hashtagName) {
        return ResponseEntity.ok(postService.getPostsByHashtag(hashtagName));
    }

    @GetMapping("/search")
    public ResponseEntity<List<PostDTO>> searchPosts(@RequestParam String query) {
        return ResponseEntity.ok(postService.searchPosts(query));
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<List<PostDTO>> getPostsByUsername(@PathVariable String username) {
        return ResponseEntity.ok(postService.getPostsByUsername(username));
    }
}