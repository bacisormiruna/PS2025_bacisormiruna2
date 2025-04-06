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

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final HashtagRepository hashtagRepository;

    @Autowired
    public PostService(PostRepository postRepository, HashtagRepository hashtagRepository) {
        this.postRepository = postRepository;
        this.hashtagRepository = hashtagRepository;
    }

    // Creare postare
    @Transactional
    public Post createPost(Long authorId, String content, String imageUrl, Set<String> hashtagNames) {
        Post post = new Post();
        post.setAuthorId(authorId);
        post.setContent(content);
        post.setImageUrl(imageUrl);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());

        Set<Hashtag> hashtags = new HashSet<>();
        for (String hashtagName : hashtagNames) {
            Hashtag hashtag = hashtagRepository.findByNameIgnoreCase(hashtagName)
                    .orElseGet(() -> {
                        Hashtag newHashtag = new Hashtag(hashtagName);
                        return hashtagRepository.save(newHashtag);
                    });
            hashtags.add(hashtag);
        }
        post.setHashtags(hashtags);

        return postRepository.save(post);
    }

    // Editare postare
    @Transactional
    public Post updatePost(Long postId, Long authorId, String newContent, String newImageUrl, Set<String> newHashtags) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found with id: " + postId));

        if (!post.getAuthorId().equals(authorId)) {
            throw new SecurityException("Only the author can edit this post");
        }

        post.setContent(newContent);
        post.setImageUrl(newImageUrl);
        post.setUpdatedAt(LocalDateTime.now());

        Set<Hashtag> updatedHashtags = new HashSet<>();
        for (String hashtagName : newHashtags) {
            Hashtag hashtag = hashtagRepository.findByNameIgnoreCase(hashtagName)
                    .orElseGet(() -> hashtagRepository.save(new Hashtag(hashtagName)));
            updatedHashtags.add(hashtag);
        }
        post.setHashtags(updatedHashtags);

        return postRepository.save(post);
    }

    // Ștergere postare
    @Transactional
    public void deletePost(Long postId, Long authorId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found with id: " + postId));

        if (!post.getAuthorId().equals(authorId)) {
            throw new SecurityException("Only the author can delete this post");
        }

        postRepository.delete(post);
    }

    // Obține toate postările (sortate după dată)
    public List<Post> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    // Filtrare după hashtag
    public List<Post> getPostsByHashtag(String hashtag) {
        return postRepository.findByHashtag(hashtag);
    }

    // Filtrare după conținut
    public List<Post> searchPosts(String keyword) {
        return postRepository.findByContentContainingIgnoreCaseOrderByCreatedAtDesc(keyword);
    }

    // Filtrare după autor
    public List<Post> getPostsByUser(Long authorId) {
        return postRepository.findByAuthorIdOrderByCreatedAtDesc(authorId);
    }
}

