package com.example.demo.builder.userbuilder;


import com.example.demo.dto.userdto.UserViewDTO;
import com.example.demo.entity.User;

import java.time.format.DateTimeFormatter;

public class UserViewBuilder {

    public static UserViewDTO generateDTOFromEntity(User user){
        return  UserViewDTO.builder()
                .name(user.getName())
                .email(user.getEmail())
                .timeStamp(user.getTimeStamp().format(DateTimeFormatter.ofPattern("MM-dd-yyy hh:mm:ss")))
                .roleName(user.getRole().getName())
                .build();
    }
}
