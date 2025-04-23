package com.example.demo.controller;

import com.example.demo.dto.postdto.PostCreateDTO;
import com.example.demo.dto.postdto.PostDTO;
import com.example.demo.dto.reactiondto.ReactionCountDTO;
import com.example.demo.dto.reactiondto.ReactionCreateDTO;
import com.example.demo.entity.Post;
import com.example.demo.enumeration.TargetType;
import com.example.demo.errorhandler.PostNotFoundException;
import com.example.demo.errorhandler.UnauthorizedException;
import com.example.demo.service.JWTService;
import com.example.demo.service.PostService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;
    @Autowired
    private JWTService jwtService;

    @GetMapping
    public ResponseEntity<List<PostDTO>> getAllPosts( @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserId(token);
        String username = jwtService.extractUsername(token);

        if (userId == null || userId <= 0) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        List<PostDTO> posts = postService.getAllPosts();
        System.out.println("Posts in controller: " + posts.size());
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

//    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<PostDTO> createPost(
//            @RequestPart("postDto") String postDtoJson,
//            @RequestPart(value = "image", required = false) MultipartFile imageFile) throws Exception {
//        try {
//            ObjectMapper objectMapper = new ObjectMapper();
//            objectMapper.registerModule(new JavaTimeModule());
//            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//
//            PostDTO postDTO = objectMapper.readValue(postDtoJson, PostDTO.class);
////pun 0 la userId ca sa nu mai fac request la baza de date
//            PostDTO createdPost = postService.createPost1(postDTO, imageFile);
//            System.out.println("Hashtags for the post: " + createdPost.getHashtags());
//            return ResponseEntity.ok(createdPost);
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new RuntimeException("Error creating post: " + e.getMessage());
//        }
//    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostDTO> createPost(
            @RequestHeader ("Authorization") String authHeader,
            @RequestPart("postCreateDto") String postDtoJson,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) throws Exception {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserId(token);
        String username = jwtService.extractUsername(token);

        if (userId == null || userId <= 0) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        PostCreateDTO postCreateDTO = objectMapper.readValue(postDtoJson, PostCreateDTO.class);

        PostDTO createdPost = postService.createPost1(userId,username, postCreateDTO, imageFile);
        return ResponseEntity.ok(createdPost);
    }


    @PutMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updatePost(
            @PathVariable Long postId,
            @RequestHeader("Authorization") String authHeader,
            @RequestPart("postCreateDto") String postCreateDtoJson,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {

        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String token = authHeader.substring(7);
            String username = jwtService.extractUsername(token);
            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            PostCreateDTO postCreateDto = objectMapper.readValue(postCreateDtoJson, PostCreateDTO.class);

            PostDTO updatedPost = postService.updatePost(postId, postCreateDto, username, imageFile);

            return ResponseEntity.ok(updatedPost);

        } catch (PostNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body("Invalid JSON format");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error updating post");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDTO> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(
            @PathVariable Long postId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String username = jwtService.extractUsername(token);

            postService.deletePost(postId, username);
            return ResponseEntity.noContent().build();

        } catch (PostNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error deleting post");
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


    @PostMapping("/user-info")
    public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token missing or invalid format");
            }
            String token = authHeader.substring(7);  // elimină "Bearer " și păstrează doar tokenul
            String username = jwtService.extractUsername(token);
            Long userId = jwtService.extractUserId(token);
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("username", username);
            userInfo.put("userId", userId);
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token: " + e.getMessage());
        }
    }

    @PostMapping("/ui")
    public ResponseEntity getUI(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token missing or invalid format");
            }
            String token = authHeader.substring(7);
            System.out.println("Extracted token: " + token);
            String username = jwtService.extractUsername(token);
            System.out.println("Extracted username: " + username);
            Long userId = jwtService.extractUserId(token);
            System.out.println("Extracted userId: " + userId);
            if (username == null || userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
            }
            return ResponseEntity.ok(userId);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<List<PostDTO>> getPostsByUser(@PathVariable String username){
        List<PostDTO> posts = postService.getPostsByUsername(username);
        return ResponseEntity.ok(posts);
    }

    @PostMapping("/{postId}/hashtags")
    public ResponseEntity<PostDTO> addHashtagsToPost(
            @PathVariable Long postId,
            @RequestParam List<String> hashtags,
            @RequestHeader("Authorization") String authHeader) {

        try {
            String token = authHeader.substring(7);
            String username = jwtService.extractUsername(token);

            PostDTO updatedPost = postService.addHashtagsToPost(postId, hashtags, username);
            return ResponseEntity.ok(updatedPost);

        } catch (PostNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/filter")
    public ResponseEntity<List<PostDTO>> filterPosts(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String hashtag) {
        List<PostDTO> posts = postService.getFilteredPosts(username, content, hashtag);
        return ResponseEntity.ok(posts);
    }


    @PostMapping("/publicByUserIds")
    public List<PostDTO> getPublicPostsByUserIds(@RequestBody List<Long> userIds) {
        return postService.findPublicPostsByUserIds(userIds);
    }

    @PostMapping("/{postId}/react")
    public ResponseEntity<Void> reactToPost(@PathVariable Long postId,
                                            @RequestBody ReactionCreateDTO dto) {
        dto.setTargetId(postId);
        dto.setTargetType(TargetType.POST);
        postService.sendReaction(dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/reactions/{id}")
    public ResponseEntity<PostDTO> getPostsWithReactions(@PathVariable Long id) {
        Post post = postService.findById(id); // sau getById, cum ai tu metoda
        PostDTO postDto = postService.getPostWithReactions(post);
        return ResponseEntity.ok(postDto);
    }
}