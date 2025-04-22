package com.example.demo.service;

import com.example.demo.dto.frienddto.FriendDTO;
import com.example.demo.dto.postdto.PostDTO;
import com.example.demo.entity.Friendship;
import com.example.demo.entity.RequestStatus;
import com.example.demo.entity.User;
import com.example.demo.errorhandler.FriendshipException;
import com.example.demo.errorhandler.UserException;
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

    public void sendFriendRequest(Long senderId, Long receiverId) throws FriendshipException, UserException {
        if (senderId.equals(receiverId)) {
            throw new FriendshipException("You cannot send a friend request to yourself");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new UserException("Sender not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new UserException("Receiver not found"));

        if (sender.getFriends().contains(receiver)) {
            throw new FriendshipException("You are already friends");
        }

        //verifică dacă există deja o cerere de prietenie în așteptare
        Optional<Friendship> exists = friendshipRepository.findBySenderAndReceiverAndStatus(sender, receiver, RequestStatus.PENDING);
        if (exists.isPresent()) {
            throw new FriendshipException("Friend request already sent");
        }

        Optional<Friendship> exists2 = friendshipRepository.findBySenderAndReceiverAndStatus(sender, receiver, RequestStatus.ACCEPTED);
        if (exists2.isPresent()) {
            throw new FriendshipException("You are already friends");
        }

        Optional<Friendship> rejectedExists = friendshipRepository.findBySenderAndReceiverAndStatus(sender, receiver, RequestStatus.REJECTED);
        if (rejectedExists.isPresent()) {
            friendshipRepository.delete(rejectedExists.get());
        }

        Friendship request = new Friendship();
        request.setSender(sender);
        request.setReceiver(receiver);
        request.setStatus(RequestStatus.PENDING);

        friendshipRepository.save(request);
        System.out.println("Friend request sent successfully");
    }

    public void acceptFriendRequest(Long senderId, Long userId) throws FriendshipException {
        Friendship friendship = friendshipRepository.findBySenderIdAndReceiverId(senderId, userId)
                .orElseThrow(() -> new FriendshipException("Friend request not found"));

        if (friendship.getStatus().equals(RequestStatus.ACCEPTED)) {
            throw new FriendshipException("You are already friends");
        }
        if (friendship.getStatus().equals(RequestStatus.REJECTED)) {
            throw new FriendshipException("You cannot accept a rejected request!");
        }

        friendship.setStatus(RequestStatus.ACCEPTED);
        friendshipRepository.save(friendship);

        User sender = friendship.getSender();
        User receiver = friendship.getReceiver();

        sender.getFriends().add(receiver);
        receiver.getFriends().add(sender);

        userRepository.save(sender);
        userRepository.save(receiver);
        System.out.println("Friend request accepted successfully");
    }

    public void rejectFriendRequest(Long senderId, Long userId) throws FriendshipException {
        Friendship friendship = friendshipRepository.findBySenderIdAndReceiverId(senderId, userId)
                .orElseThrow(() -> new FriendshipException("Friend request not found"));

        if (friendship.getStatus().equals(RequestStatus.ACCEPTED)) {
            throw new FriendshipException("You cannot reject an accepted request!");
        }
        if (friendship.getStatus().equals(RequestStatus.REJECTED)) {
            throw new FriendshipException("You already rejected this request!");
        }

        friendship.setStatus(RequestStatus.REJECTED);
        friendshipRepository.save(friendship);
        System.out.println("Friend request rejected");
    }

    public List<FriendDTO> getFriends(Long userId) throws UserException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("User not found"));

        return user.getFriends().stream()
                .map(friend -> new FriendDTO(friend.getId(), friend.getName(), "ACCEPTED"))
                .collect(Collectors.toList());
    }

    public Long getAuthenticatedUserId() throws UserException {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            User user = userRepository.findByName(username);
            return user.getId();
        }
        throw new UserException("User not authenticated");
    }

    public List<FriendDTO> getFriendshipRequests() throws UserException {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            throw new UserException("User not authenticated");
        }

        String username = ((UserDetails) principal).getUsername();
        User user = userRepository.findByName(username);

        List<Friendship> pendingRequests = friendshipRepository.findByReceiverAndStatus(user, RequestStatus.PENDING);
        List<Friendship> acceptedRequests = friendshipRepository.findByReceiverAndStatus(user, RequestStatus.ACCEPTED);
        List<Friendship> rejectedRequests = friendshipRepository.findByReceiverAndStatus(user, RequestStatus.REJECTED);

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

    public List<Long> getFriendIds(Long userId) throws UserException {
        return getFriends(userId).stream()
                .map(FriendDTO::getId)
                .collect(Collectors.toList());
    }


}

