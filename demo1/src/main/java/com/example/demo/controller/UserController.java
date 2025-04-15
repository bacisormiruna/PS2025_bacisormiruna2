package com.example.demo.controller;

import com.example.demo.dto.hashtagdto.HashtagDTO;
import com.example.demo.dto.postdto.PostCreateDTO;
import com.example.demo.dto.postdto.PostDTO;
import com.example.demo.dto.userdto.UserDTO;
import com.example.demo.entity.User;
import com.example.demo.errorhandler.FriendshipException;
import com.example.demo.errorhandler.UserException;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.JWTService;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.mysql.cj.conf.PropertyKey.logger;

@RestController
@CrossOrigin
@RequestMapping(value = "/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final WebClient webClientBuilder;
    private final JWTService jwtService;

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

//afisarea postarilor
    @RequestMapping(value = "/getAllPosts", method = RequestMethod.GET)
    public ResponseEntity<?> displayAllPosts(){
        return new ResponseEntity<>(userService.getPostsFromM2(), HttpStatus.OK);
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

//---------------actualizarea unei postari ---------------------------


    //--------------crearea unei postari------------------------
    @PutMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            path = "/updatePost"
    )
    public ResponseEntity<?> updatePost(
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
            PostDTO createdPost = userService.updatePost(userId, username, postCreateDTO, imageFile, authHeader);
            return ResponseEntity.ok(createdPost);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating post: " + e.getMessage());
        }
    }





}
