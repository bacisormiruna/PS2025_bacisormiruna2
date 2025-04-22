package com.example.demo.repository;

import com.example.demo.dto.postdto.PostDTO;
import com.example.demo.entity.Post;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByOrderByCreatedAtDesc();
    List<Post> findByUsernameOrderByCreatedAtDesc(String username);
    List<Post> findByUsername(String username);

    @Query("SELECT p FROM Post p JOIN p.hashtags h WHERE h.name = :hashtagName ORDER BY p.createdAt DESC")
    List<Post> findByHashtagName(@Param("hashtagName") String hashtagName);

    @Query("SELECT p FROM Post p WHERE p.content LIKE %:query% ORDER BY p.createdAt DESC")
    List<Post> searchByText(@Param("query") String query);

    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN p.hashtags h " +
            "WHERE (:username IS NULL OR p.username = :username) " +
            "AND (:content IS NULL OR p.content LIKE %:content%) " +
            "AND (:hashtag IS NULL OR h.name = :hashtag) " +
            "ORDER BY p.createdAt DESC")
    List<Post> findByFilters(
            @Param("username") String username,
            @Param("content") String content,
            @Param("hashtag") String hashtag);

    @EntityGraph(attributePaths = {"comments", "hashtags"})
    @Query("SELECT p FROM Post p WHERE p.id = :postId")
    Optional<Post> findByIdWithCommentsAndHashtags(@Param("postId") Long postId);

    @EntityGraph(attributePaths = {"comments"})
    Optional<Post> findCommentById(Long id);
    List<Post> findByAuthorIdInAndIsPublicTrue(List<Long> authorIds);
    List<Post> findAllByAuthorIdInAndIsPublicFalse(List<Long> authorIds);

}