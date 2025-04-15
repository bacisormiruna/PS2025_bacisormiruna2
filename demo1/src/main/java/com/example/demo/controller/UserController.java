package com.example.demo.controller;

import com.example.demo.dto.hashtagdto.HashtagDTO;
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

import java.util.Optional;

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

//crearea de postari
    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE, // Important pentru Multipart
            value = "/createPost"
    )
    public ResponseEntity<Mono<PostDTO>> createPost(
            @RequestPart("postDto") PostDTO postDTO,  // Folosește @RequestPart pentru DTO
            @RequestPart("image") MultipartFile imageFile  // Primește fișierul
    ) throws UserException {
        Long userId = userService.getAuthenticatedUserId();
        return ResponseEntity.ok(userService.createPost(userId,postDTO,imageFile));
    }

    @GetMapping("/check-auth")
    public ResponseEntity<String> checkAuth(@AuthenticationPrincipal UserDetails userDetails) throws UserException {
        if (userDetails != null) {
            return ResponseEntity.ok("User logat: " + userService.getAuthenticatedUsername() + "cu id-ul: "+userService.getAuthenticatedUserId());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Nu este autentificat");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Long> getMyUserId(@AuthenticationPrincipal UserDetails userDetails) throws UserException {
        if (userDetails != null) {
            Long userId = userService.getAuthenticatedUserId();  // metoda ta
            return ResponseEntity.ok(userId);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
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







}
