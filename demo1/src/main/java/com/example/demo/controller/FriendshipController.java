package com.example.demo.controller;

import com.example.demo.dto.frienddto.FriendDTO;
import com.example.demo.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friendship")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendshipService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/send/{receiverId}")
    public ResponseEntity<?> sendFriendRequest(@PathVariable Long receiverId) {
        Long senderId = friendshipService.getAuthenticatedUserId();
        friendshipService.sendFriendRequest(senderId, receiverId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/accept/{requestId}")
    public ResponseEntity<?> acceptFriendRequest(@PathVariable Long requestId) {
        Long userId = friendshipService.getAuthenticatedUserId();
        friendshipService.acceptFriendRequest(requestId, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/reject/{senderId}")
    public ResponseEntity<?> rejectFriendRequest(@PathVariable Long senderId) {
       Long userId = friendshipService.getAuthenticatedUserId();
       friendshipService.rejectFriendRequest(senderId, userId);
       return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/friends")
    public ResponseEntity<List<FriendDTO>> getFriends() {
        Long userId = friendshipService.getAuthenticatedUserId();
        List<FriendDTO> friends = friendshipService.getFriends(userId);
        return new ResponseEntity<>(friends, HttpStatus.OK);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/requests")
    public List<FriendDTO> getFriendshipRequests() {
        return friendshipService.getFriendshipRequests();
    }
}
