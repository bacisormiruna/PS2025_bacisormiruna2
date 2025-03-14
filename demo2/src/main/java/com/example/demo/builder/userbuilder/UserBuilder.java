package com.example.demo.builder.userbuilder;


import com.example.demo.dto.userdto.UserDTO;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;

import java.time.LocalDateTime;

public class UserBuilder {

    public static User generateEntityFromDTO(UserDTO userDTO, Role role){
        return  User.builder().id(userDTO.getId())
                .name(userDTO.getName())
                .email(userDTO.getEmail())
                .password(userDTO.getPassword())
                .timeStamp(LocalDateTime.now())
                .role(role)
                .build();
    }
}
