package com.example.demo.service;

import com.example.demo.dto.hashtagdto.HashtagDTO;
import com.example.demo.dto.postdto.PostCreateDTO;
import com.example.demo.dto.postdto.PostDTO;
import com.example.demo.entity.Hashtag;
import com.example.demo.entity.Post;
import com.example.demo.errorhandler.PostNotFoundException;
import com.example.demo.errorhandler.UnauthorizedException;
import com.example.demo.mapper.HashtagMapper;
import com.example.demo.mapper.PostMapper;
import com.example.demo.repository.HashtagRepository;
import com.example.demo.repository.PostRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.h2.mvstore.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private HashtagRepository hashtagRepository;
    @Autowired
    private JWTService jwtService;

    @Autowired
    private PostMapper postMapper;
    @Autowired
    private HashtagService hashtagService;
    @Autowired
    private HashtagMapper hashtagMapper;

    public List<PostDTO> getAllPosts() {
        List<Post> posts = postRepository.findAllByOrderByCreatedAtDesc();
        System.out.println("Found posts: " + posts.size()); // Log pentru a verifica câte postări sunt returnate
        return posts.stream()
                .map(postMapper::toDto)
                .collect(Collectors.toList());
    }


//    public PostDTO createPost1(Long userId, PostDTO postDto, MultipartFile imageFile) throws Exception {
//        Post post = postMapper.toEntity(postDto);
//        post.setCreatedAt(LocalDateTime.now());
//        post.setAuthorId(userId);
//
//
//        Set<Hashtag> hashtags = extractHashtags(postDto.getHashtags());
//        System.out.println("Hashtags extrase: " +postDto.getHashtags());
//        System.out.println("Hashtags extrase: " + hashtags);
//
//        post.setHashtags(hashtags);
//        Post savedPost = postRepository.save(post);
//
//        if (imageFile != null && !imageFile.isEmpty()) {
//            storeImage(savedPost.getId(), imageFile);
//        }
//        return postMapper.toDto(savedPost);
//    }


    @Transactional
    public PostDTO createPost1(Long userId, String username, PostCreateDTO postCreateDto, MultipartFile imageFile) throws Exception {
        Post post = postMapper.toEntity(postCreateDto, userId, username);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        post.setAuthorId(userId);
        post.setUsername(username);
        if (postCreateDto.getHashtags() != null && !postCreateDto.getHashtags().isEmpty()) {
            Set<Hashtag> hashtags = new HashSet<>();

            for (HashtagDTO hashtagDto : postCreateDto.getHashtags()) {
                String hashtagName = hashtagDto.getName();
                if (hashtagName != null && !hashtagName.trim().isEmpty()) {
                    Hashtag hashtag = hashtagService.findOrCreateHashtag(hashtagName);
                    hashtags.add(hashtag);
                }
            }
            post.setHashtags(hashtags);
        }
        Post savedPost = postRepository.save(post);
        if (imageFile != null && !imageFile.isEmpty()) {
            storeImage(savedPost.getId(), imageFile);
        }
        return postMapper.toDto(savedPost);
    }

    public PostDTO getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException("Post not found with id: " + id));
        return postMapper.toDto(post);
    }


    @Transactional
    public PostDTO updatePost(Long postId, PostCreateDTO postCreateDto, String username, MultipartFile imageFile) throws Exception {
        Post existingPost = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found with id: " + postId));

        if (!existingPost.getUsername().equals(username)) {
            throw new UnauthorizedException("You can only update your own posts");
        }

        existingPost.setContent(postCreateDto.getContent());
        existingPost.setUpdatedAt(LocalDateTime.now());
        existingPost.setIsPublic(postCreateDto.getIsPublic());
        existingPost.setUsername(username);
        if (postCreateDto.getHashtags() != null) {
            Set<Hashtag> updatedHashtags = new HashSet<>();
            for (HashtagDTO hashtagDto : postCreateDto.getHashtags()) {
                String hashtagName = hashtagDto.getName();
                if (hashtagName != null && !hashtagName.trim().isEmpty()) {
                    Hashtag hashtag = hashtagService.findOrCreateHashtag(hashtagName);
                    updatedHashtags.add(hashtag);
                }
            }
            existingPost.setHashtags(updatedHashtags);
        } else {
            existingPost.getHashtags().clear();
        }
        if (imageFile != null && !imageFile.isEmpty()) {
            storeImage(existingPost.getId(), imageFile);
        }
        Post updatedPost = postRepository.save(existingPost);
        return postMapper.toDto(updatedPost);
    }

    @Transactional
    public void deletePost(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found with id: " + postId));

        if (!post.getUsername().equals(username)) {
            throw new UnauthorizedException("You can only delete your own posts");
        }

        post.getHashtags().clear();
        postRepository.saveAndFlush(post);

        postRepository.delete(post);
    }

    public List<PostDTO> getPostsByHashtag(String hashtagName) {
        return postRepository.findByHashtagName(hashtagName).stream()
                .map(postMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    protected Set<Hashtag> extractHashtags(Set<HashtagDTO> hashtagsDTO) {
        Set<Hashtag> hashtags = new HashSet<>();
        if (hashtagsDTO == null) {
            return hashtags;
        }

        for (HashtagDTO hashtagDTO : hashtagsDTO) {
            String hashtagName = hashtagDTO.getName();
            if (hashtagName == null || hashtagName.trim().isEmpty()) {
                continue;
            }
            if (!hashtagName.startsWith("#")) {
                hashtagName = "#" + hashtagName;
            }

            Hashtag hashtag = null;
            try {
                hashtag = hashtagRepository.findByName(hashtagName)
                        .orElse(null);
            } catch (Exception e) {
                System.err.println("Error finding hashtag: " + e.getMessage());
            }

            if (hashtag == null) {
                try {
                    hashtag = new Hashtag();
                    hashtag.setName(hashtagName);
                    hashtag = hashtagRepository.save(hashtag);
                    System.out.println("Created new hashtag with ID: " + hashtag.getId());
                } catch (Exception e) {
                    System.err.println("Error creating hashtag: " + e.getMessage());
                    continue;
                }
            }
            hashtags.add(hashtag);
        }
        return hashtags;
    }

    public List<PostDTO> searchPosts(String query) {
        return postRepository.searchByText(query).stream()
                .map(postMapper::toDto)
                .collect(Collectors.toList());
    }

//    public List<PostDTO> getPostsByUsername(String username) {
//        return postRepository.findByUsername(username).stream()
//                .map(postMapper::toDto)
//                .collect(Collectors.toList());
//    }

    @Transactional
    public List<PostDTO> getPostsByUsername(String username) {
        List<Post> posts = postRepository.findByUsernameOrderByCreatedAtDesc(username);

        return posts.stream()
                .map(postMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public PostDTO storeImage(Long postId, MultipartFile imageFile) throws Exception {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found with id: " + postId));
        byte[] imageData = imageFile.getBytes();
        post.setImage(imageData);
        post.setImageUrl(post.getImageUrl());//optional
        Post updatedPost = postRepository.save(post);
        return postMapper.toDto(updatedPost);
    }

    @Transactional
    public PostDTO addHashtagsToPost(Long postId, List<String> hashtagNames, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found with id: " + postId));
        if (!post.getUsername().equals(username)) {
            throw new UnauthorizedException("You can only modify your own posts");
        }
        Set<Hashtag> newHashtags = new HashSet<>();
        for (String hashtagName : hashtagNames) {
            if (hashtagName != null && !hashtagName.trim().isEmpty()) {
                Hashtag hashtag = hashtagService.findOrCreateHashtag(hashtagName);
                newHashtags.add(hashtag);
            }
        }

        post.getHashtags().addAll(newHashtags);
        post.setUpdatedAt(LocalDateTime.now());
        Post updatedPost = postRepository.save(post);

        return postMapper.toDto(updatedPost);
    }

    public List<PostDTO> getFilteredPosts(String username, String content, String hashtag) {
        String normalizedHashtag = (hashtag != null && !hashtag.startsWith("#")) ? "#" + hashtag : hashtag;

        List<Post> posts = postRepository.findByFilters(
                username,
                content,
                normalizedHashtag);

        return posts.stream()
                .map(postMapper::toDto)
                .collect(Collectors.toList());
    }

}