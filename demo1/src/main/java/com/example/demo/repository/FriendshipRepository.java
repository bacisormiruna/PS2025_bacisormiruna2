package com.example.demo.repository;

import com.example.demo.entity.Friendship;
import com.example.demo.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    List<Friendship> findByReceiverAndStatus(User receiver, Friendship.RequestStatus status);
    Optional<Friendship> findBySenderAndReceiverAndStatus(User sender, User receiver,Friendship.RequestStatus status);
    Optional<Friendship> findBySenderIdAndReceiverId(Long senderId, Long receiverId);
}
