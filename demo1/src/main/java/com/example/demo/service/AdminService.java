package com.example.demo.service;

import com.example.demo.builder.userbuilder.UserBuilder;
import com.example.demo.dto.userdto.UserDTO;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.errorhandler.UserException;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.validator.UserFieldValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminService {

    @Autowired
    private final UserRepository userRepository;

    public void blockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<User> admins = userRepository.findUserByRoleName("ADMIN");
        if (admins.contains(user)) {
            throw new RuntimeException("Admin users cannot be blocked");
        }

        user.setBlocked(true);
        userRepository.save(user);
    }

    public void unblockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<User> admins = userRepository.findUserByRoleName("ADMIN");
        if (admins.contains(user)) {
            throw new RuntimeException("Admin users cannot be unblocked");
        }

        user.setBlocked(false);
        userRepository.save(user);
    }

}
