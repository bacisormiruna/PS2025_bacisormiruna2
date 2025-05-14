package com.example.demo.service;

import com.example.demo.builder.userbuilder.UserBuilder;
import com.example.demo.builder.userbuilder.UserViewBuilder;
import com.example.demo.dto.commentdto.CommentCreateDTO;
import com.example.demo.dto.commentdto.CommentDTO;
import com.example.demo.dto.frienddto.FriendDTO;
import com.example.demo.dto.notificationdto.NotificationDTO;
import com.example.demo.dto.postdto.PostCreateDTO;
import com.example.demo.dto.postdto.PostDTO;
import com.example.demo.dto.reactiondto.ReactionCreateDTO;
import com.example.demo.dto.userdto.UserDTO;
import com.example.demo.dto.userdto.UserViewDTO;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.entity.UserPrincipal;
import com.example.demo.enumeration.NotificationType;
import com.example.demo.errorhandler.*;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService{

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final FriendshipService friendshipService;
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    private final JWTService jwtService;
    private AuthenticationManager authenticationManager;
    private final WebClient webClientBuilder;
    private final WebClient moderatorClient;
    private final ObjectMapper objectMapper;

    public UserService(RoleRepository roleRepository,
                       UserRepository userRepository,
                       FriendshipService friendshipService,
                       AuthenticationManager authenticationManager,
                       JWTService jwtService,
                       @Qualifier("postsMicroserviceClient") WebClient postsClient,
                       @Qualifier("moderatorMicroserviceClient") WebClient moderatorClient,
                       ObjectMapper objectMapper) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.friendshipService = friendshipService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.webClientBuilder = postsClient;
        this.moderatorClient = moderatorClient;
        this.objectMapper = objectMapper;
    }

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

    public void changeUserRole(Long id, String roleName) throws UserException {
        User user = userRepository.findById(id).orElseThrow(() -> new UserException("User not found"));
        Role role = roleRepository.findRoleByName(roleName).orElseThrow(() -> new UserException("Role not found"));
        user.setRole(role);
        userRepository.save(user);
    }

    public Flux<PostDTO> getPostsFromM2(String authHeader) {
        //checkIfUserBlocked(jwtService.extractUserId(authHeader));
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
       // checkIfUserBlocked(userId);
        return webClientBuilder.post()
                .uri("/api/posts")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header("Authorization", authHeader)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(PostDTO.class)
                .block();
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
        //checkIfUserBlocked(userId);
        return webClientBuilder.put()
                .uri("/api/posts/" + postId)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header("Authorization", authHeader)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(PostDTO.class)
                .block();
    }

    public void deletePost(Long postId, String username, Long userId, String authHeader) {
        try {
            String userRole = getUserRoleById(userId);

            if (userRole.equals("moderator")) {
                webClientBuilder.delete()
                        .uri("/api/posts/" + postId)
                        .header("Authorization", authHeader)
                        .retrieve()
                        .toBodilessEntity()
                        .block();
            } else {
                //checkIfUserBlocked(userId);
                PostDTO postDTO = getPostDTOById(postId);
                if (postDTO.getUsername().equals(username)) {
                    webClientBuilder.delete()
                            .uri("/api/posts/" + postId)
                            .header("Authorization", authHeader)
                            .retrieve()
                            .toBodilessEntity()
                            .block();
                } else {
                    throw new UnauthorizedException("You can only delete your own posts");
                }
            }
        } catch (WebClientResponseException.NotFound e) {
            throw new PostNotFoundException("Post not found with id: " + postId);
        } catch (WebClientResponseException.Forbidden e) {
            throw new UnauthorizedException("You can only delete your own posts");
        } catch (Exception e) {
            throw new RuntimeException("Error when deleting post: " + e.getMessage(), e);
        }
    }

    public List<PostDTO> getPostsByUserFromOtherService(String username) {
        //checkIfUserBlockedByUsername(username);
        return webClientBuilder.get()
                .uri("/api/posts/user/{username}", username)  // Apelăm endpoint-ul din al doilea microserviciu
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
            //checkIfUserBlocked(jwtService.extractUserId(token));
            return webClientBuilder.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/posts/{postId}/hashtags")
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

    public List<PostDTO> filterPosts(String username, String content, String hashtag) {
        System.out.println("UserService: Filtering posts with username=" + username + ", content=" + content + ", hashtag=" + hashtag);
        //checkIfUserBlockedByUsername(username);
        try {
            return webClientBuilder
                    .get()
                    .uri(uriBuilder -> {
                        UriBuilder builder = uriBuilder.path("/api/posts/filter");
                        if (username != null && !username.isEmpty()) {
                            builder.queryParam("username", username);
                        }
                        if (content != null && !content.isEmpty()) {
                            builder.queryParam("content", content);
                        }
                        if (hashtag != null && !hashtag.isEmpty()) {
                            builder.queryParam("hashtag", hashtag);
                        }
                        return builder.build();
                    })
                    .retrieve()
                    .bodyToFlux(PostDTO.class)
                    .collectList()
                    .block();
        } catch (Exception e) {
            System.err.println("Error calling post service: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }
    public List<PostDTO> getMyAndFriendsPosts(Long userId) throws UserException {
        List<Long> friendIds = friendshipService.getFriendIds(userId);
        friendIds.add(userId);
        //checkIfUserBlocked(userId);
        return webClientBuilder.post()
                .uri("/api/posts/publicByUserIds")
                .bodyValue(friendIds)
                .retrieve()
                .bodyToFlux(PostDTO.class)
                .collectList()
                .block();
    }

    public CommentDTO createCommentForPost(Long postId, CommentCreateDTO commentCreateDto, String token) {
        //checkIfUserBlocked(jwtService.extractUserId(token));
        return webClientBuilder.post()
                .uri("/api/comments/addComment/{postId}", postId)
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

    public CommentDTO updateCommentForPost(Long postId, CommentCreateDTO commentCreateDto, String token) {
       // checkIfUserBlocked(jwtService.extractUserId(token));
        return webClientBuilder.put()
                .uri("/api/comments/updateComment/{postId}", postId)
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


    public void deleteCommentFromPost(Long commentId, String username, Long userId, String authHeader) {
        try {
            String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : null;
            if (token == null) {
                throw new UnauthorizedException("Missing or invalid token.");
            }
            String userRole = getUserRoleById(userId);
            System.out.println("User role: " + userRole);
            if (userRole.equals("moderator")) {
                webClientBuilder
                        .delete()
                        .uri(uriBuilder -> uriBuilder
                                .path("/api/comments/deleteComment/" + commentId)
                                .queryParam("moderatorAction", "true")
                                .build())
                        .header("Authorization", "Bearer " + token)
                        .retrieve()
                        .toBodilessEntity()
                        .block();
                return;
            }
            CommentDTO commentDTO = getCommentDTOById(commentId);
            if (commentDTO.getUsername().equals(username)) {
                webClientBuilder
                        .delete()
                        .uri("/api/comments/deleteComment/" + commentId)
                        .header("Authorization", "Bearer " + token)
                        .retrieve()
                        .toBodilessEntity()
                        .block();
            } else {
                throw new UnauthorizedException("You can only delete your own comments");
            }
        } catch (WebClientResponseException.NotFound e) {
            throw new CommentNotFoundException("Comment not found with id: " + commentId);
        } catch (WebClientResponseException.Forbidden e) {
            throw new UnauthorizedException("You are not authorized to delete this comment");
        } catch (Exception e) {
            throw new RuntimeException("Error when deleting comment: " + e.getMessage(), e);
        }
    }


    public void sendReactionToPost(Long postId, ReactionCreateDTO dto, String token) {
        if (dto.getUserId() == null) {
            Long userId = jwtService.extractUserId(token);
            dto.setUserId(userId);
        }
        webClientBuilder
                .post()
                .uri("/api/posts/{postId}/reactToPost", postId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .bodyValue(dto)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException("Error from post service: " + body)))
                )
                .toBodilessEntity()
                .block();
    }

    public void sendReactionToComm(Long commentId, ReactionCreateDTO dto, String token) {
        if (dto.getUserId() == null) {
            Long userId = jwtService.extractUserId(token);
            dto.setUserId(userId);
        }
        webClientBuilder
                .post()
                .uri("/api/posts/{commentId}/reactToComm", commentId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .bodyValue(dto)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException("Error from post service: " + body)))
                )
                .toBodilessEntity()
                .block();
    }

    public PostDTO getPostWithReactions(Long postId, String token) {
        return webClientBuilder
                .get()
                .uri("/api/posts/reactions/{id}", postId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException("Error from post service: " + body)))
                )
                .bodyToMono(PostDTO.class)
                .block();
    }

    public List<PostDTO> getAllPostsWithReactions(String token) {
        return webClientBuilder
                .get()
                .uri("/api/posts/getAllPostsWithReactions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException("Error from post service: " + body)))
                )
                .bodyToFlux(PostDTO.class)
                .collectList()
                .block();
    }

    public String getUserRoleById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user.getRole().getName();
    }

    public PostDTO getPostDTOById(Long postId) {
        return webClientBuilder.get()
                .uri("/api/posts/{id}", postId)
                .retrieve()
                .bodyToMono(PostDTO.class)
                .block();
    }

    public CommentDTO getCommentDTOById(Long commentId) {
        return webClientBuilder.get()
                .uri("/api/comments/{id}", commentId)
             //   .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken) // Asigură-te că folosești tokenul corect
                .retrieve()
                .bodyToMono(CommentDTO.class)
                .block();
    }

//    public void checkIfUserBlocked(Long userId) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//
//        if (user.isBlocked()) {
//            throw new UserBlockedException("Contul a fost blocat");
//        }
//    }

    public void checkIfUserBlocked(Long userId) throws UserException {
        try {
            ResponseEntity<Boolean> response =
                    moderatorClient.get()
                    .uri("/api/validator/users/{userId}/isBlocked", userId)
                    .retrieve()
                    .toEntity(Boolean.class)
                    .block();

            if (response != null && response.getBody() != null && response.getBody()) {
                throw new UserException("Your account has been blocked. Please contact support for assistance.");
            }
        } catch (WebClientResponseException e) {
            // În caz de eroare de comunicare, permitem login-ul pentru a nu bloca utilizatorii
            //log.error("Error checking if user is blocked: {}", e.getMessage());
        }
    }




}


