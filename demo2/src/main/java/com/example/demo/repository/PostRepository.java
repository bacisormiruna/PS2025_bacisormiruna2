package com.example.demo.repository;

import com.example.demo.dto.postdto.PostDTO;
import com.example.demo.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByOrderByCreatedAtDesc();

    List<Post> findByUsername(String username);

    @Query("SELECT p FROM Post p JOIN p.hashtags h WHERE h.name = :hashtagName ORDER BY p.createdAt DESC")
    List<Post> findByHashtagName(@Param("hashtagName") String hashtagName);

    @Query("SELECT p FROM Post p WHERE p.content LIKE %:query% ORDER BY p.createdAt DESC")
    List<Post> searchByText(@Param("query") String query);
}