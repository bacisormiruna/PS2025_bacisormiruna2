package com.example.demo.controller;
import com.example.demo.dto.commentdto.CommentCreateDTO;
import com.example.demo.dto.commentdto.CommentDTO;
import com.example.demo.dto.postdto.PostCreateDTO;
import com.example.demo.dto.postdto.PostDTO;
import com.example.demo.dto.reactiondto.ReactionCreateDTO;
import com.example.demo.dto.userdto.UserDTO;
import com.example.demo.entity.User;
import com.example.demo.errorhandler.PostNotFoundException;
import com.example.demo.errorhandler.UnauthorizedException;
import com.example.demo.errorhandler.UserException;
import com.example.demo.service.JWTService;
import com.example.demo.service.UserService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


@RestController
@CrossOrigin
@RequestMapping(value = "/api/user")
public class UserController {

    private final WebClient webClient3;
    private final WebClient webClient2;
    private final UserService userService;
    private final JWTService jwtService;

    public UserController(@Qualifier("moderatorMicroserviceClient") WebClient webClient3, @Qualifier("postsMicroserviceClient") WebClient webClient2,UserService userService,JWTService jwtService ) {
        this.webClient3 = webClient3;
        this.webClient2 = webClient2;
        this.userService=userService;
        this.jwtService=jwtService;
    }

    @RequestMapping(value = "/getAll", method = RequestMethod.GET)
    public ResponseEntity<?> displayAllUserView(){
        return new ResponseEntity<>(userService.findAllUserView(), HttpStatus.OK);
    }

    @RequestMapping(value = "/getUserById/{id}", method = RequestMethod.GET)
    public ResponseEntity<?> displayUserViewById(@PathVariable("id") @NonNull  Long id) throws UserException {
        return new ResponseEntity<>(userService.findUserViewById(id), HttpStatus.OK);
    }

    @RequestMapping(value = "/getUserByEmail/{email}", method = RequestMethod.GET)
    public ResponseEntity<?> displayUserViewByEmail(@PathVariable("email") String email) throws UserException {
        return new ResponseEntity<>(userService.findUserViewByEmail(email), HttpStatus.OK);
    }

    @RequestMapping(value = "/getUserByRoleName/{roleName}", method = RequestMethod.GET)
    public ResponseEntity<?> displayUserViewByRoleName(@PathVariable("roleName") String roleName) throws UserException {
        return new ResponseEntity<>(userService.findUserViewByRoleName(roleName), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, value = "/create")
    public ResponseEntity<?> processAddUserForm(@RequestBody(required = false) UserDTO userDTO) throws UserException {
        return new ResponseEntity<>(userService.createUser(userDTO), HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserDTO loginDTO) {
        try {
            String token = userService.verify(loginDTO);
            userService.checkIfUserBlocked(jwtService.extractUserId(token));
            return ResponseEntity.ok(token);
        } catch (UserException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @RequestMapping(method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, value = "/update")
    public ResponseEntity<?> processUpdateUserForm(@RequestBody UserDTO userDTO) throws UserException {
        return new ResponseEntity<>(userService.updateUser(userDTO), HttpStatus.OK);
    }

    @RequestMapping(value="/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteUserByIdForm(@PathVariable("id") Long id) throws UserException {
        userService.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/getAllPosts")
    public Mono<ResponseEntity<?>> displayAllPosts(@RequestHeader("Authorization") String auth) {
        return userService.getPostsFromM2(auth)
                .collectList()
                .map(posts -> ResponseEntity.ok(posts));
    }


    @PostMapping("/user-info")
    public ResponseEntity getUserInfo(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token missing or invalid format");
            }
            String token = authHeader.substring(7);  // elimină "Bearer " și păstrează doar tokenul
            System.out.println("Extracted token: " + token);  // Adaugă un print pentru debugging
            String username = jwtService.extractUsername(token);  // Extrage username-ul din token
            System.out.println("Extracted username: " + username);  // Verifică username-ul
            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
            }
            User user = userService.findByName(username);  // Căutare în baza de date
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
            Long userId = user.getId();  // Obține userId din obiectul User
            System.out.println("Found userId: " + userId);  // Verifică userId-ul găsit
            return ResponseEntity.ok(userId);
        } catch (Exception e) {
            e.printStackTrace();  // Adaugă un print pentru a verifica eroarea
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
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


//--------------crearea unei postari------------------------
    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            path = "/createPosts"
    )
    public ResponseEntity<?> createPost(
            @RequestHeader("Authorization") String authHeader,
            @RequestPart("postCreateDto") PostCreateDTO postCreateDTO,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {

        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Token missing or invalid format");
            }

            String token = authHeader.substring(7);
            String username = jwtService.extractUsername(token);
            Long userId = jwtService.extractUserId(token);

            if (username == null || userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid token");
            }
            PostDTO createdPost = userService.createPost(userId, username, postCreateDTO, imageFile, authHeader);
            return ResponseEntity.ok(createdPost);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating post: " + e.getMessage());
        }
    }

    //--------------actualizarea unei postari------------------------
    @PutMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            path = "/updatePosts/{postId}"
    )
    public ResponseEntity<?> updatePost(
            @PathVariable Long postId,
            @RequestHeader("Authorization") String authHeader,
            @RequestPart("postCreateDto") PostCreateDTO postCreateDTO,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {

        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Token missing or invalid format");
            }

            String token = authHeader.substring(7);
            String username = jwtService.extractUsername(token);
            Long userId = jwtService.extractUserId(token);

            if (username == null || userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid token");
            }

            PostDTO updatedPost = userService.updatePost(postId, userId, username, postCreateDTO, imageFile, authHeader);
            return ResponseEntity.ok(updatedPost);

        } catch (PostNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating post: " + e.getMessage());
        }
    }
//stergerea unei postari
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<?> deletePost(
            @PathVariable Long postId,
            @RequestHeader("Authorization") String authHeader) {

        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Token missing or invalid format");
            }

            String token = authHeader.substring(7);
            String username = jwtService.extractUsername(token);
            Long userId = jwtService.extractUserId(token);

            if (username == null || userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid token");
            }

            userService.deletePost(postId, username, userId, authHeader);
            return ResponseEntity.noContent().build();

        } catch (PostNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting post: " + e.getMessage());
        }
    }


    @GetMapping("/{username}/posts")
    public ResponseEntity<List<PostDTO>> getPostsByUser(@PathVariable String username) {
        try {
            List<PostDTO> posts = userService.getPostsByUserFromOtherService(username);
            return ResponseEntity.ok(posts);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/posts/{postId}/hashtags")
    public ResponseEntity<PostDTO> addHashtagsToUserPost(
            @PathVariable Long postId,
            @RequestParam List<String> hashtags,
            @RequestHeader("Authorization") String authHeader) {

        try {
            String token = authHeader.substring(7);
            String username = jwtService.extractUsername(token);

            PostDTO updatedPost = userService.addHashtagsToUserPost(postId, hashtags, username, token);
            return ResponseEntity.ok(updatedPost);

        } catch (PostNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/addComment/{postId}")
    public ResponseEntity<?> addCommentThroughUserService(
            @PathVariable Long postId,
            @RequestBody CommentCreateDTO commentCreateDto,
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token.");
        }

        try {
            CommentDTO createdComment = userService.createCommentForPost(postId, commentCreateDto, authHeader.substring(7));
            return ResponseEntity.ok(createdComment);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/updateComment/{postId}")
    public ResponseEntity<?> updateCommentThroughUserService(
            @PathVariable Long postId,
            @RequestBody CommentCreateDTO commentCreateDto,
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token.");
        }

        try {
            CommentDTO createdComment = userService.updateCommentForPost(postId, commentCreateDto, authHeader.substring(7));
            return ResponseEntity.ok(createdComment);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/deleteComment/{commentId}")
    public ResponseEntity<?> deleteCommentForUser(
            @PathVariable Long commentId,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token.");
        }
        try {
            String token = authHeader.substring(7);
            String username = jwtService.extractUsername(token);
            Long userId = jwtService.extractUserId(token);
            System.out.println("Extracted username: " + username);  // Adaugă un log pentru a verifica
            System.out.println("Extracted userId: " + userId);
            userService.deleteCommentFromPost(commentId, username, userId, authHeader);
            return ResponseEntity.noContent().build();

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/filterPosts")
    public ResponseEntity<List<PostDTO>> filterPosts(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String hashtag) {
        System.out.println("Filtering posts for username: " + username);
        System.out.println("Filtering posts for content: " + content);
        System.out.println("Filtering posts for hashtag: " + hashtag);
        List<PostDTO> posts = userService.filterPosts(username, content, hashtag);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/myfeed")
    public ResponseEntity<List<PostDTO>> getMyAndFriendsFeed(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ArrayList<>());
            }
            String token = authHeader.substring(7);
            Long userId = jwtService.extractUserId(token);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ArrayList<>());
            }
            List<PostDTO> posts = userService.getMyAndFriendsPosts(userId);
            posts.sort((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()));
            return ResponseEntity.ok(posts);
        } catch (UserException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ArrayList<>());
        }
    }

    @PostMapping("/reactToPost/{postId}")
    public ResponseEntity<?> reactToPostThroughUserService(
            @PathVariable Long postId,
            @RequestBody ReactionCreateDTO reactionCreateDto,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token.");
        }

        try {
            String token = authHeader.substring(7);
            userService.sendReactionToPost(postId, reactionCreateDto, token);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/reactToComm/{commentId}")
    public ResponseEntity<?> reactToCommThroughUserService(
            @PathVariable Long commentId,
            @RequestBody ReactionCreateDTO reactionCreateDto,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token.");
        }

        try {
            String token = authHeader.substring(7);
            userService.sendReactionToComm(commentId, reactionCreateDto, token);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/getPostWithReactions/{postId}")
    public ResponseEntity<?> getPostWithReactionsThroughUserService(
            @PathVariable Long postId,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token.");
        }

        try {
            String token = authHeader.substring(7);
            PostDTO postWithReactions = userService.getPostWithReactions(postId, token);
            return ResponseEntity.ok(postWithReactions);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/getAllPostsWithReactions")
    public ResponseEntity<?> getAllPostsWithReactionsThroughUserService(
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token.");
        }

        try {
            String token = authHeader.substring(7);
            List<PostDTO> posts = userService.getAllPostsWithReactions(token);
            return ResponseEntity.ok(posts);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/verify/{userId}")
    public ResponseEntity<String> verifyUser(@PathVariable Long userId,
                                             @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token.");
        }

        String token = authHeader.substring(7); // extragem tokenul
        System.out.println("Token extras în verifyUser: " + token); // log ajutător
        //Long id= jwtService.extractUserId(token);
        System.out.println("Id-ul în verifyUser: " + jwtService.extractUserId(token));

        try {
            String userRole = userService.getUserRoleById(userId).toLowerCase();
            if (userRole.equals("moderator")) {
                return ResponseEntity.ok("Moderator");
            }else if (userRole.equals("user")) {
                return ResponseEntity.ok("User");
            }
            else if (userRole.equals("admin")) {
                return ResponseEntity.ok("Admin");
            }
            else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Utilizator neautorizat");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/checkModerator")
    public ResponseEntity<String> checkIfModerator(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token.");
        }
        String token = authHeader;
        Boolean isModerator = webClient3
                .get()
                .uri("/api/validator/isModerator")
                .header("Authorization", token)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();

        if (Boolean.TRUE.equals(isModerator)) {
            return ResponseEntity.ok("DA, utilizatorul este moderator.");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("NU, utilizatorul nu este moderator.");
        }
    }

    @PutMapping("/users/{userId}/block")
    public Mono<ResponseEntity<String>> blockUser(
            @PathVariable Long userId,
            @RequestBody String reason,
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token."));
        }
        String token = authHeader.substring(7);
        return webClient3
                .put()
                .uri("/api/validator/users/{userId}/block", userId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(reason)
                .exchangeToMono(response -> {
                    HttpStatusCode statusCode = response.statusCode();
                    return response.bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .flatMap(body -> {
                                if (statusCode.is2xxSuccessful()) {
                                    return Mono.just(ResponseEntity.ok("User successfully blocked"));
                                } else if (statusCode.is4xxClientError()) {
                                    if (body.contains("already blocked")) {
                                        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                                .body("User with ID " + userId + " is already blocked."));
                                    } else {
                                        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                                .body("Client error: " + body));
                                    }
                                } else if (statusCode.is5xxServerError()) {
                                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                            .body("Server error: " + body));
                                } else {
                                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                            .body("Unexpected response: " + body));
                                }
                            });
                })
                .onErrorResume(e -> {
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Unexpected error: " + e.getMessage()));
                });
    }


    @PutMapping("/users/{userId}/unblock")
    public Mono<ResponseEntity<String>> unblockUser(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token."));
        }
        String token = authHeader.substring(7);
        return webClient3
                .put()
                .uri("/api/validator/users/{userId}/unblock", userId) // Adresăm cererea către microserviciul de deblocare
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    return ResponseEntity.ok("User successfully unblocked");
                })
                .onErrorResume(e -> {
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage()));
                });
    }

    @PostMapping("/delete-post-from-m1")
    public Mono<ResponseEntity<String>> deletePostFromM1(@RequestParam Long postId,
                                                         @RequestParam Long authorId,
                                                         @RequestParam String reason,
                                                         @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token."));
        }
        String token = authHeader.substring(7);
        return webClient3
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/validator/delete-post")
                        .queryParam("postId", postId)
                        .queryParam("authorId", authorId)
                        .queryParam("reason", reason)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchangeToMono(response -> {
                    HttpStatusCode statusCode = response.statusCode();
                    return response.bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .flatMap(body -> {
                                if (statusCode.is2xxSuccessful()) {
                                    return Mono.just(ResponseEntity.ok("Post successfully marked as deleted"));
                                } else if (statusCode.is4xxClientError()) {
                                    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error deleting post: " + body));
                                } else if (statusCode.is5xxServerError()) {
                                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error: " + body));
                                } else {
                                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected response: " + body));
                                }
                            });
                })
                .onErrorResume(e -> {
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error: " + e.getMessage()));
                });
    }


    @PostMapping("/delete-comment-from-m1")
    public Mono<ResponseEntity<String>> deleteCommentFromM1(@RequestParam Long commentId,
                                                            @RequestParam Long authorId,
                                                            @RequestParam String reason,
                                                            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token."));
        }
        String token = authHeader.substring(7);
        return webClient3
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/validator/delete-comment")
                        .queryParam("commentId", commentId)
                        .queryParam("authorId", authorId)
                        .queryParam("reason", reason)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchangeToMono(response -> {
                    HttpStatusCode statusCode = response.statusCode();
                    return response.bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .flatMap(body -> {
                                if (statusCode.is2xxSuccessful()) {
                                    return Mono.just(ResponseEntity.ok("Comment successfully marked as deleted"));
                                } else if (statusCode.is4xxClientError()) {
                                    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error deleting comment: " + body));
                                } else if (statusCode.is5xxServerError()) {
                                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error: " + body));
                                } else {
                                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected response: " + body));
                                }
                            });
                })
                .onErrorResume(e -> {
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error: " + e.getMessage()));
                });
    }

    @GetMapping("/notifications")
    public Mono<ResponseEntity<List>> getNotifications(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        String token = authHeader.substring(7);
        Long userId;
        try {
            userId = jwtService.extractUserId(token);
        } catch (Exception e) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyList()));
        }
        return webClient3
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/validator/activity-log")
                        .build(userId))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(List.class)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList())));
    }


}
