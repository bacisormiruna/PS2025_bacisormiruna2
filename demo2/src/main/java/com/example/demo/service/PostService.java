package com.example.demo.service;

import com.example.demo.dto.hashtagdto.HashtagDTO;
import com.example.demo.dto.postdto.PostDTO;
import com.example.demo.entity.Hashtag;
import com.example.demo.entity.Post;
import com.example.demo.errorhandler.PostNotFoundException;
import com.example.demo.repository.HashtagRepository;
import com.example.demo.repository.PostRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final HashtagRepository hashtagRepository;

    @Autowired
    public PostService(PostRepository postRepository, HashtagRepository hashtagRepository) {
        this.postRepository = postRepository;
        this.hashtagRepository = hashtagRepository;
    }

    @Transactional
    public Post addPost(Long userId, String content, String imageUrl, Set<String> hashtags) {
        Set<Hashtag> hashtagSet = new HashSet<>();
        for (String hashtagName : hashtags) {
            Hashtag hashtag = hashtagRepository.findByNameIgnoreCase(hashtagName)
                    .orElseGet(() -> hashtagRepository.save(new Hashtag(hashtagName)));
            hashtagSet.add(hashtag);
        }

        Post post = new Post();
        post.setAuthorId(userId);
        post.setContent(content);
        post.setImageUrl(imageUrl);
        post.setHashtags(hashtagSet);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());

        return postRepository.save(post);
    }

    //stergere
    @Transactional
    public void deletePost(Long postId, Long authorId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found with id: " + postId));

        if (!post.getAuthorId().equals(authorId)) {
            throw new SecurityException("Only the author can delete this post");
        }
        postRepository.delete(post);
    }
    //actualizare
    @Transactional
    public Post updatePost(Long postId, Long authorId, String newContent, String newImageUrl, Set<String> hashtags) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found with id: " + postId));

        if (!post.getAuthorId().equals(authorId)) {
            throw new SecurityException("Only the author can edit this post");
        }

        post.setContent(newContent);
        post.setImageUrl(newImageUrl);
        post.setUpdatedAt(LocalDateTime.now());
        Set<Hashtag> hashtagSet = new HashSet<>();
        for (String hashtagName : hashtags) {
            Hashtag hashtag = hashtagRepository.findByNameIgnoreCase(hashtagName)
                    .orElseGet(() -> hashtagRepository.save(new Hashtag(hashtagName)));
            hashtagSet.add(hashtag);
        }
        post.setHashtags(hashtagSet);
        return postRepository.save(post);
    }
    //cautare
    public List<Post> searchPosts(String keyword) {
        return postRepository.findByContentContainingIgnoreCase(keyword);
    }

    //afisare
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public List<Post> getPostsByUser(Long authorId) {
        return postRepository.findByAuthorId(authorId);
    }

    public List<Post> getPostsByHashtag(String hashtag) {
        return postRepository.findByHashtagsName(hashtag);
    }

    public List<PostDTO> getPostsByHashtagDTO(String hashtag) {
        List<Post> posts = postRepository.findByHashtagsName(hashtag);
        return posts.stream()
                .map(post -> new PostDTO(post.getId(), post.getAuthorId(), post.getContent(), post.getImageUrl(), post.getCreatedAt(), post.getUpdatedAt()))
                .collect(Collectors.toList());
    }

    public List<HashtagDTO> getHashtagsByPostId(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found with id: " + postId));
        return post.getHashtags().stream()
                .map(hashtag -> new HashtagDTO(hashtag.getId(), hashtag.getName()))
                .collect(Collectors.toList());
    }

    public Post getPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found with id: " + postId));
    }

    public List<Post> getPostsByAuthorId(Long authorId) {
        return postRepository.findByAuthorId(authorId);
    }

    public List<Post> getPostsByHashtags(Set<String> hashtags) {
        return postRepository.findByHashtagsNameIn(hashtags);
    }
    public List<Post> getPostsByContent(String content) {
        return postRepository.findByContentContainingIgnoreCase(content);
    }

    public String storeImage(MultipartFile image) {
        if (image == null || image.isEmpty()) return null;

        try {
            String filename = UUID.randomUUID() + "_" + image.getOriginalFilename();
            Path imagePath = Paths.get("uploads/" + filename); // Creează un folder „uploads” în proiect
            Files.createDirectories(imagePath.getParent());
            Files.copy(image.getInputStream(), imagePath, StandardCopyOption.REPLACE_EXISTING);
            return "/images/" + filename; // Poți face să servești aceste fișiere statice
        } catch (IOException e) {
            throw new RuntimeException("Failed to store image", e);
        }
    }

}