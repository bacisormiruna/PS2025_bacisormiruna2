package com.example.demo.repository;

import com.example.demo.dto.postdto.PostDTO;
import com.example.demo.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    // Metode de bază
    List<Post> findAllByOrderByCreatedAtDesc();

    // Filtrare după autor
    List<Post> findByAuthorIdOrderByCreatedAtDesc(Long authorId);

    // Filtrare după conținut
    List<Post> findByContentContainingIgnoreCase(String keyword);

    // Filtrare după hashtag
    List<Post> findByHashtagsName(String hashtagName);

    // Filtrare după multiple hashtag-uri
    @Query("SELECT DISTINCT p FROM Post p JOIN p.hashtags h WHERE h.name IN :hashtags")
    List<Post> findByHashtagsNameIn(@Param("hashtags") Set<String> hashtags);

    // Filtrare după vizibilitate
    List<Post> findByIsPublicOrderByCreatedAtDesc(boolean isPublic);

    // Filtre combinate
    List<Post> findByAuthorIdAndIsPublicOrderByCreatedAtDesc(Long authorId, boolean isPublic);

    List<Post> findByAuthorIdAndContentContainingIgnoreCase(Long authorId, String keyword);

    // Metode pentru statistici sau alte operațiuni speciale
    @Query("SELECT p FROM Post p WHERE p.authorId = :authorId AND YEAR(p.createdAt) = YEAR(:date)")
    List<Post> findByAuthorIdAndYear(@Param("authorId") Long authorId, @Param("date") LocalDateTime date);

    boolean existsByIdAndAuthorId(Long postId, Long authorId);

    // Metodă optimizată pentru a evita N+1 query probleme
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.hashtags WHERE p.id = :id")
    Optional<Post> findByIdWithHashtags(@Param("id") Long id);

    List<Post> findByAuthorId(Long authorId);
}