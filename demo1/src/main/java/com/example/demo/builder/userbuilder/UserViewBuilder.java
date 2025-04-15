package com.example.demo.builder.userbuilder;


import com.example.demo.dto.userdto.UserViewDTO;
import com.example.demo.entity.User;

import java.time.format.DateTimeFormatter;

public class UserViewBuilder {

    public static UserViewDTO generateDTOFromEntity(User user) {
        if (user.getTimeStamp() == null) {
            throw new IllegalArgumentException("User timestamp cannot be null");
        }
        return UserViewDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .timeStamp(user.getTimeStamp().format(DateTimeFormatter.ofPattern("MM-dd-yyy hh:mm:ss")))
                .roleName(user.getRole().getName())
                .build();
    }

}
