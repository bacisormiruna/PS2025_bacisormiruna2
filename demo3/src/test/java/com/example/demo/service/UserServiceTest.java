package com.example.demo.service;

import com.example.demo.dto.userdto.UserDTO;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.errorhandler.UserException;
import com.example.demo.repository.UserRepository;
import com.example.demo.utils.DataTest;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UserServiceTest extends BaseTest{

    @Resource
    DataTest dataTest;

    @Resource
    UserService userService;

    @Resource
    UserRepository userRepository;

    @Test
    public void test_createUser_throwsForInvalidUser()
    {
        assertThatThrownBy(() -> userService.createUser(null)).isInstanceOf(UserException.class);
    }

    @Test
    public void test_createUser_successForValidUser() throws UserException{

        Role role = dataTest.createRole("ADMIN");

        UserDTO userDTO = UserDTO.builder().email("email").name("name1").password("password").roleName(role.getName()).build();

        Long id = userService.createUser(userDTO);

        User user = userRepository.findById(id).get();

        assertThat(user.getRole()).isEqualTo(role);
        assertThat(user.getEmail()).isEqualTo(userDTO.getEmail());
        assertThat(user.getName()).isEqualTo(userDTO.getName());
    }

    @Test
    public void test_editUser_successForValidUser() throws UserException{

        Role role = dataTest.createRole("ADMIN");
        User user = dataTest.createUser("name", "email", "password", role);

        User cratedUser = userRepository.findById(user.getId()).get();

        assertThat(cratedUser.getPassword()).isEqualTo("password");
        assertThat(cratedUser.getEmail()).isEqualTo("email");
        assertThat(cratedUser.getName()).isEqualTo("name");

        UserDTO userDTO = UserDTO.builder().id(cratedUser.getId()).email("email1").name("name1").password("password1").roleName(role.getName()).build();

        Long id = userService.updateUser(userDTO);

        User updatedUser = userRepository.findById(id).get();

        assertThat(updatedUser.getPassword()).isEqualTo("password1");
        assertThat(updatedUser.getEmail()).isEqualTo("email1");
        assertThat(updatedUser.getName()).isEqualTo("name1");
    }
}

