package com.example.demo.controller;

import com.example.demo.builder.userbuilder.UserBuilder;
import com.example.demo.dto.userdto.UserDTO;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.errorhandler.UserException;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AdminService;
import com.example.demo.service.JWTService;
import com.example.demo.service.UserService;
import com.example.demo.validator.UserFieldValidator;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin
@RequestMapping(value = "/api/admin")
@RequiredArgsConstructor
public class AdminController{

    private final UserService userService;
    private final AdminService adminService;
    private final JWTService jwtService;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);


    @RequestMapping(value = "/getAll", method = RequestMethod.GET)
    public ResponseEntity<?> displayAllUserView(){
        return new ResponseEntity<>(userService.findAllUserView(), HttpStatus.OK);
    }

    @RequestMapping(value = "/getUserById/{id}", method = RequestMethod.GET)
    public ResponseEntity<?> displayUserViewById(@PathVariable("id") @NonNull Long id) throws UserException {
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

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, value = "/login")
    public String loginForm(@RequestBody(required = false) UserDTO userDTO) throws UserException {
        return userService.verify2(userDTO);
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
    @RequestMapping(value="/changeRole/{id}/{role}", method = RequestMethod.PUT)
    public ResponseEntity<?> changeUserRole(@PathVariable("id") Long id, @PathVariable("role") String role) throws UserException {
        userService.changeUserRole(id, role);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(value = "/createUserOrModerator", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createUserOrModerator(@RequestBody UserDTO userDTO,
                                                @RequestHeader("Authorization") String token) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token missing or invalid format");
            }
            String extractedToken = token.substring(7);
            System.out.println("Extracted token: " + extractedToken); // Debugging print
            String username = jwtService.extractUsername(extractedToken);
            System.out.println("Extracted username: " + username); // Debugging print

            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
            }

            User currentUser = userRepository.findByName(username);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
            }
            Role currentRole = currentUser.getRole();
            if (currentRole == null || !currentRole.getName().equalsIgnoreCase("admin")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You must be an admin to create a moderator");
            }

            List<String> errors = UserFieldValidator.validateInsertOrUpdate(userDTO);
            if (!errors.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(StringUtils.collectionToDelimitedString(errors, "\n"));
            }
            Optional<Role> role = roleRepository.findRoleByName(userDTO.getRoleName().toUpperCase());
            if (role.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Role not found with name field: " + userDTO.getRoleName().toUpperCase());
            }
            Optional<User> existingUser = userRepository.findUserByEmail(userDTO.getEmail());
            if (existingUser.isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User already exists with email: " + userDTO.getEmail());
            }
            User userToSave = UserBuilder.generateEntityFromDTO(userDTO, role.get());
            userToSave.setPassword(encoder.encode(userToSave.getPassword()));
            Long userId = userRepository.save(userToSave).getId();
            return ResponseEntity.status(HttpStatus.CREATED).body(userId);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }
    }
}
