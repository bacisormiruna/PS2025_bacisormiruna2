package com.example.demo.controller;

import com.example.demo.dto.frienddto.FriendDTO;
import com.example.demo.errorhandler.FriendshipException;
import com.example.demo.errorhandler.UserException;
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
    public ResponseEntity<?> sendFriendRequest(@PathVariable Long receiverId) throws UserException, FriendshipException {
        Long senderId = friendshipService.getAuthenticatedUserId();
        friendshipService.sendFriendRequest(senderId, receiverId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/accept/{senderId}")
    public ResponseEntity<?> acceptFriendRequest(@PathVariable Long senderId) throws UserException, FriendshipException {
        Long userId = friendshipService.getAuthenticatedUserId();
        friendshipService.acceptFriendRequest(senderId, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/reject/{senderId}")
    public ResponseEntity<?> rejectFriendRequest(@PathVariable Long senderId) throws UserException, FriendshipException {
        Long userId = friendshipService.getAuthenticatedUserId();
        friendshipService.rejectFriendRequest(senderId, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/friends")
    public ResponseEntity<List<FriendDTO>> getFriends() throws UserException {
        Long userId = friendshipService.getAuthenticatedUserId();
        List<FriendDTO> friends = friendshipService.getFriends(userId);
        return new ResponseEntity<>(friends, HttpStatus.OK);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/requests")
    public ResponseEntity<List<FriendDTO>> getFriendshipRequests() throws UserException {
        List<FriendDTO> requests = friendshipService.getFriendshipRequests();
        return new ResponseEntity<>(requests, HttpStatus.OK);
    }
}
