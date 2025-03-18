package com.example.demo.service;

import com.example.demo.dto.frienddto.FriendDTO;
import com.example.demo.entity.Friendship;
import com.example.demo.entity.User;
import com.example.demo.repository.FriendshipRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    public void sendFriendRequest(Long senderId, Long receiverId) {
        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("You cannot send a friend request to yourself");
        }
        User sender = userRepository.findById(senderId).orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findById(receiverId).orElseThrow(() -> new RuntimeException("Receiver not found"));

        //vreau sa verific daca exista deja o cerere de prietenie in asteptare
        Optional<Friendship> exists = friendshipRepository.findBySenderAndReceiverAndStatus(sender,receiver,Friendship.RequestStatus.PENDING);
        if (exists.isPresent()) {
            throw new IllegalArgumentException("Friend request already sent");
        }

        Optional<Friendship> exists2 = friendshipRepository.findBySenderAndReceiverAndStatus(sender,receiver,Friendship.RequestStatus.ACCEPTED);
        if (exists2.isPresent()) {
            throw new IllegalArgumentException("You are already friends");
        }
        Optional<Friendship> rejectedExists = friendshipRepository.findBySenderAndReceiverAndStatus(sender, receiver, Friendship.RequestStatus.REJECTED);
        if (rejectedExists.isPresent()) {
            friendshipRepository.delete(rejectedExists.get());
        }

        Friendship request = new Friendship();
        request.setSender(sender);
        request.setReceiver(receiver);
        request.setStatus(Friendship.RequestStatus.PENDING);

        friendshipRepository.save(request);
        System.out.println("Friend request sent successfully");
    }

    public void acceptFriendRequest(Long requestId, Long userId) {
        Friendship friendship = friendshipRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (!friendship.getReceiver().getId().equals(userId)) {
            throw new RuntimeException("You are not authorized to accept this request");
        }

        if (friendship.getStatus() == Friendship.RequestStatus.ACCEPTED) {
            throw new RuntimeException("You are already friends");
        }
        if (friendship.getStatus() == Friendship.RequestStatus.REJECTED) {
            throw new RuntimeException("You cannot accept a rejected request!");
        }

        friendship.setStatus(Friendship.RequestStatus.ACCEPTED);
        friendshipRepository.save(friendship);

        User sender = friendship.getSender();
        User receiver = friendship.getReceiver();

        sender.getFriends().add(receiver);
        receiver.getFriends().add(sender);

        userRepository.save(sender);
        userRepository.save(receiver);
        System.out.println("Friend request accepted successfully");
    }

    public void rejectFriendRequest(Long requestId, Long userId) {
        Friendship friendship = friendshipRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (!friendship.getReceiver().getId().equals(userId)) {
            throw new RuntimeException("You are not authorized to reject this request");
        }

        if (friendship.getStatus() == Friendship.RequestStatus.ACCEPTED) {
            throw new RuntimeException("You cannot reject an accepted request!");
        }
        if (friendship.getStatus() == Friendship.RequestStatus.REJECTED) {
            throw new RuntimeException("You already rejected this request!");
        }

        friendship.setStatus(Friendship.RequestStatus.REJECTED);
        friendshipRepository.save(friendship);
        System.out.println("Friend request rejected");
    }


    public List<FriendDTO> getFriends(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return user.getFriends().stream()
                .map(friend -> new FriendDTO(friend.getId(), friend.getName(), "ACCEPTED"))
                .collect(Collectors.toList());
    }

    public Long getAuthenticatedUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            User user = userRepository.findByName(username);
            return user.getId();
        }
        throw new RuntimeException("User not authenticated");
    }


    public List<FriendDTO> getFriendshipRequests() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            throw new RuntimeException("User not authenticated");
        }

        String username = ((UserDetails) principal).getUsername();
        User user = userRepository.findByName(username);

        List<Friendship> pendingRequests = friendshipRepository.findByReceiverAndStatus(user, Friendship.RequestStatus.PENDING);
        List<Friendship> acceptedRequests = friendshipRepository.findByReceiverAndStatus(user, Friendship.RequestStatus.ACCEPTED);
        List<Friendship> rejectedRequests = friendshipRepository.findByReceiverAndStatus(user, Friendship.RequestStatus.REJECTED);

        List<FriendDTO> friendDTOs = new ArrayList<>();
        pendingRequests.forEach(request ->
                friendDTOs.add(new FriendDTO(request.getSender().getId(), request.getSender().getName(), "PENDING"))
        );
        acceptedRequests.forEach(request ->
                friendDTOs.add(new FriendDTO(request.getSender().getId(), request.getSender().getName(), "ACCEPTED"))
        );
        rejectedRequests.forEach(request ->
                friendDTOs.add(new FriendDTO(request.getSender().getId(), request.getSender().getName(), "REJECTED"))
        );
        return friendDTOs;
    }
}

