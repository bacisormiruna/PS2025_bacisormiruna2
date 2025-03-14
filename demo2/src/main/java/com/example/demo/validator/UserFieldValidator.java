package com.example.demo.validator;

import com.example.demo.dto.userdto.UserDTO;

import java.util.ArrayList;
import java.util.List;

public class UserFieldValidator {


    public static List<String> validateInsertOrUpdate(UserDTO userDTO) {

        List<String> errors = new ArrayList<>();

        if (userDTO == null) {
            errors.add("userDTO is null");
        }
        else
        {
            if (userDTO.getName() == null) {
                errors.add("User name has invalid format");
            }

            if (userDTO.getEmail() == null) {
                errors.add("User email has invalid format");
            }

            if (userDTO.getPassword() == null) {
                errors.add("User password has invalid format");
            }

            if (userDTO.getRoleName() == null) {
                errors.add("User role name has invalid format");
            }

        }

        return errors;
    }
}
