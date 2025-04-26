package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.entity.UserPrincipal;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;


@Service
public class MyUserDetailsService implements UserDetailsService{

    @Autowired
    private UserRepository repo;

//    @Override
//    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
//        User user = repo.findByName(name);
//        if(user == null){
//            System.out.println("User not Found");
//            throw new UsernameNotFoundException("User not found");
//        }
//        return new UserPrincipal(user);
//    }
    @Override
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
        User user = repo.findByName(name);
        if (user == null) {
            System.out.println("User not Found");
            throw new UsernameNotFoundException("User not found");
        }

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(user.getRole().getName());
        return new UserPrincipal(user, Collections.singletonList(authority));
    }

//    public UserDetails loadUserByUserId(Long userId) throws UsernameNotFoundException {
//        return repo.findById(userId)
//                .map(UserPrincipal::new)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//    }
    public UserDetails loadUserByUserId(Long userId) throws UsernameNotFoundException {
        return repo.findById(userId)
                .map(user -> new UserPrincipal(user, Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName().toUpperCase()))))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

}
