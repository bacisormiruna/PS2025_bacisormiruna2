package com.example.demo.utils;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataTest {

    @Resource
    private RoleRepository roleRepository;

    @Resource
    private UserRepository userRepository ;

    public Role createRole(String roleName)
    {
        Role role = Role.builder().name(roleName).build();
        return roleRepository.save(role);
    }

    public User createUser(String name, String email, String password, Role role)
    {
        User user = User.builder().name(name).email(email).password(password).role(role).timeStamp(LocalDateTime.now()).build();
        return userRepository.save(user);
    }
}