package com.example.demo.service.client;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "user-service", url = "${m1.service.url}")
public interface FriendServiceClient {

    @GetMapping("/friends/check")
    boolean checkFriendship(@RequestParam Long userId1,
                            @RequestParam Long userId2,
                            @RequestHeader("Authorization") String token);

    @GetMapping("/users/{userId}/friends")
    List<Long> getFriendIds(@PathVariable Long userId,
                            @RequestHeader("Authorization") String token);
}