package com.example.demo.service;

import com.example.demo.builder.userbuilder.UserBuilder;
import com.example.demo.builder.userbuilder.UserViewBuilder;
import com.example.demo.dto.commentdto.CommentCreateDTO;
import com.example.demo.dto.commentdto.CommentDTO;
import com.example.demo.dto.postdto.PostCreateDTO;
import com.example.demo.dto.postdto.PostDTO;
import com.example.demo.dto.userdto.UserDTO;
import com.example.demo.dto.userdto.UserViewDTO;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.entity.UserPrincipal;
import com.example.demo.errorhandler.PostNotFoundException;
import com.example.demo.errorhandler.UnauthorizedException;
import com.example.demo.errorhandler.UserException;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.validator.UserFieldValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService{

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JWTService jwtService;

    @Autowired
    private final WebClient webClientBuilder;
    private final ObjectMapper objectMapper;

    public List<UserViewDTO> findAllUserView() {

        return userRepository.findAll().stream()
                .map(UserViewBuilder::generateDTOFromEntity)
                .collect(Collectors.toList());
    }

    public UserViewDTO findUserViewById(Long id) throws UserException {

        Optional<User> user = userRepository.findById(id);

        if (user.isEmpty()) {
            throw new UserException("User not found with id field: " + id);
        }
        return UserViewBuilder.generateDTOFromEntity(user.get());
    }

    public UserViewDTO findUserViewByEmail(String email) throws UserException {
        Optional<User> user = userRepository.findUserByEmail(email);

        if (user.isEmpty()) {
            throw new UserException("User not found with email field: " + email);
        }
        return UserViewBuilder.generateDTOFromEntity(user.get());
    }

    public Long createUser(UserDTO userDTO) throws UserException {
        List<String> errors = UserFieldValidator.validateInsertOrUpdate(userDTO);

        if(!errors.isEmpty())
        {
            throw new UserException(StringUtils.collectionToDelimitedString(errors, "\n"));
        }

        Optional<Role> role = roleRepository.findRoleByName(userDTO.getRoleName().toUpperCase());

        if (role.isEmpty()) {
            throw new UserException("Role not found with name field: " + userDTO.getRoleName().toUpperCase());
        }

        Optional<User> user = userRepository.findUserByEmail(userDTO.getEmail());
        if(user.isPresent() ){
            throw new UserException("User record does not permit duplicates for email field: " + userDTO.getEmail());
        }

        User userSave = UserBuilder.generateEntityFromDTO(userDTO, role.get());
        userSave.setPassword(encoder.encode(userSave.getPassword()));

        return userRepository.save(userSave).getId();
    }

    public User findByName(String name) {
        return userRepository.findByName(name);
    }

    public Long updateUser(UserDTO userDTO) throws UserException {
        List<String> errors = UserFieldValidator.validateInsertOrUpdate(userDTO);

        if(!errors.isEmpty())
        {
            throw new UserException(StringUtils.collectionToDelimitedString(errors, "\n"));
        }

        Optional<Role> role = roleRepository.findRoleByName(userDTO.getRoleName().toUpperCase());

        if (role.isEmpty()) {
            throw new UserException("Role not found with name field: " + userDTO.getRoleName().toUpperCase());
        }
        Optional<User> user = userRepository.findById(userDTO.getId());
        if(user.isEmpty()){
            throw new UserException("User not found with id field: " + userDTO.getId());
        }
        if(!user.get().getEmail().equals(userDTO.getEmail()))
        {
            Optional<User> verifyDuplicated = userRepository.findUserByEmail(userDTO.getEmail());
            if(verifyDuplicated.isPresent() ){
                throw new UserException("User record does not permit duplicates for email field: " + userDTO.getEmail());
            }
        }
        user.get().setName(userDTO.getName());
        user.get().setEmail(userDTO.getEmail());
        user.get().setPassword(userDTO.getPassword());
        user.get().setRole(role.get());

        return userRepository.save(user.get()).getId();
    }
    public void deleteUser(Long id) throws UserException {

        Optional<User> user = userRepository.findById(id);

        if (user.isEmpty()) {
            throw new UserException("User not found with id field: " + id);
        }
        this.userRepository.deleteById(id);
    }

    public List<UserViewDTO> findUserViewByRoleName(String roleName) throws UserException {
        List<User> userList  = userRepository.findUserByRoleName(roleName);
        if (userList.isEmpty()) {
            throw new UserException("User not found with role name field: " + roleName);
        }
        return  userList.stream()
                .map(UserViewBuilder::generateDTOFromEntity)
                .collect(Collectors.toList());
    }

//    public String verify(UserDTO userDTO) throws UserException {
//        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userDTO.getName(),userDTO.getPassword()));
//        if(authentication.isAuthenticated())
//            return jwtService.generateToken(userDTO);
//        return "fail";
//    }

    public String verify(UserDTO loginDTO) throws UserException {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getName(), loginDTO.getPassword())
        );

        if (!authentication.isAuthenticated()) {
            throw new UserException("Login failed. Invalid credentials.");
        }

        User user = userRepository.findByName(loginDTO.getName());
        if (user == null) {
            throw new UserException("User not found");
        }

        UserDTO userDTO = UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .roleName(user.getRole().getName())
                .build();
        return jwtService.generateToken(userDTO);
    }

    public String verify2(UserDTO userDTO) throws UserException {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userDTO.getName(), userDTO.getPassword())
        );

        if (!authentication.isAuthenticated()) {
            throw new UserException("Login failed. Invalid credentials.");
        }
        User user = userRepository.findByName(userDTO.getName());
        if (!"ADMIN".equalsIgnoreCase(user.getRole().getName())) {
            throw new UserException("Access denied. Only admins can log in.");
        }
        return jwtService.generateToken(userDTO);
    }


    public Long getAuthenticatedUserId() throws UserException {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            User user = userRepository.findByName(username);
            return user.getId();
        }
        throw new UserException("User not authenticated");
    }

    public String getAuthenticatedUsername() throws UserException {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            User user = userRepository.findByName(username);
            return user.getName();
        }
        throw new UserException("User not authenticated");
    }


    public void changeUserRole(Long id, String roleName) throws UserException {
        User user = userRepository.findById(id).orElseThrow(() -> new UserException("User not found"));
        Role role = roleRepository.findRoleByName(roleName).orElseThrow(() -> new UserException("Role not found"));
        user.setRole(role);
        userRepository.save(user);
    }

    public Flux<PostDTO> getPostsFromM2(String authHeader) {
        return webClientBuilder
                .get()
                .uri("/api/posts")
                .header("Authorization", authHeader)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> {
                    if (response.statusCode() == HttpStatus.UNAUTHORIZED) {
                        return Mono.error(new RuntimeException("Unauthorized access"));
                    }
                    return Mono.error(new RuntimeException("Error fetching posts"));
                })
                .bodyToFlux(PostDTO.class);
    }
//createPost
    public PostDTO createPost(
            Long userId,
            String username,
            PostCreateDTO postCreateDTO,
            MultipartFile imageFile,
            String authHeader) throws Exception {

        MultipartBodyBuilder builder = new MultipartBodyBuilder();

        Map<String, Object> postData = new HashMap<>();
        postData.put("content", postCreateDTO.getContent());
        postData.put("isPublic", postCreateDTO.getIsPublic());
        postData.put("hashtags", postCreateDTO.getHashtags());

        builder.part("postCreateDto", objectMapper.writeValueAsString(postData))
                .contentType(MediaType.APPLICATION_JSON);

        if (imageFile != null && !imageFile.isEmpty()) {
            builder.part("image", imageFile.getResource())
                    .filename(imageFile.getOriginalFilename());
        }

        return webClientBuilder.post()
                .uri("/api/posts") // Sau numele din service discovery
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header("Authorization", authHeader)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(PostDTO.class)
                .block(); // Folosim block() pentru că avem nevoie de răspuns sincron
    }

//updatePost
    public PostDTO updatePost(
            Long postId,
            Long userId,
            String username,
            PostCreateDTO postCreateDTO,
            MultipartFile imageFile,
            String authHeader) throws Exception {

        MultipartBodyBuilder builder = new MultipartBodyBuilder();

        Map<String, Object> postData = new HashMap<>();
        postData.put("content", postCreateDTO.getContent());
        postData.put("isPublic", postCreateDTO.getIsPublic());
        postData.put("hashtags", postCreateDTO.getHashtags());

        builder.part("postCreateDto", objectMapper.writeValueAsString(postData))
                .contentType(MediaType.APPLICATION_JSON);

        if (imageFile != null && !imageFile.isEmpty()) {
            builder.part("image", imageFile.getResource())
                    .filename(imageFile.getOriginalFilename());
        }

        return webClientBuilder.put()
                .uri("/api/posts/" + postId) // Sau numele din service discovery
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header("Authorization", authHeader)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(PostDTO.class)
                .block();
    }

    //deletePost
    public void deletePost(Long postId, String username, Long userId, String authHeader) {
        try {
            webClientBuilder.delete()
                    .uri("/api/posts/" + postId)
                    .header("Authorization", authHeader)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            throw new PostNotFoundException("Post not found with id: " + postId);
        } catch (WebClientResponseException.Forbidden e) {
            throw new UnauthorizedException("You can only delete your own posts");
        } catch (Exception e) {
            throw new RuntimeException("Error when deleting post: " + e.getMessage(), e);
        }
    }

    //--sa adaug hashtaguri

    //sa adaug partea de getFeed
    public List<PostDTO> getPostsByUserFromOtherService(String username) {
        return webClientBuilder.get()
                .uri("api/posts/user/{username}", username)  // Apelăm endpoint-ul din al doilea microserviciu
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException("Client error: " + errorBody))))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException("Server error: " + errorBody))))
                .bodyToMono(new ParameterizedTypeReference<List<PostDTO>>() {})
                .block();
    }

    public PostDTO addHashtagsToUserPost(Long postId, List<String> hashtags, String username, String token) {
        try {
            return webClientBuilder.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("api/posts/{postId}/hashtags")
                            .build(postId))
                    .header("Authorization", "Bearer " + token)
                    .bodyValue(hashtagsToParams(hashtags))
                    .retrieve()
                    .onStatus(HttpStatusCode::is5xxServerError, response -> {
                        if (response.statusCode() == HttpStatus.NOT_FOUND) {
                            return Mono.error(new PostNotFoundException("Post not found: " + postId));
                        } else if (response.statusCode() == HttpStatus.FORBIDDEN) {
                            return Mono.error(new UnauthorizedException("You cannot modify this post."));
                        }
                        return Mono.error(new RuntimeException("Client error: " + response.statusCode()));
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, response ->
                            Mono.error(new RuntimeException("Server error: " + response.statusCode()))
                    )
                    .bodyToMono(PostDTO.class)
                    .block();

        } catch (Exception e) {
            throw new RuntimeException("Error while adding hashtags: " + e.getMessage(), e);
        }
    }

    private MultiValueMap<String, String> hashtagsToParams(List<String> hashtags) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("hashtags", hashtags);
        return params;
    }



    public CommentDTO createCommentForPost(Long postId, CommentCreateDTO commentCreateDto, String token) {

        return webClientBuilder.post()
                .uri("api/comments/addComment/{postId}", postId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .bodyValue(commentCreateDto)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> clientResponse.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new RuntimeException("Client error: " + errorBody))))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> clientResponse.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new RuntimeException("Server error: " + errorBody))))
                .bodyToMono(CommentDTO.class)
                .block();
    }

    public List<PostDTO> filterPosts(String username, String content, String hashtag) {
        return webClientBuilder
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/posts/filter")  // Endpoint-ul corect al al doilea microserviciu
                        .queryParam("username", username)
                        .queryParam("content", content)
                        .queryParam("hashtag", hashtag)
                        .build())
                .retrieve()
                .bodyToFlux(PostDTO.class)
                .collectList()
                .block();
    }






}


