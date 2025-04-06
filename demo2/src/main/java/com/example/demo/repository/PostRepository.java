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
    // Toate postările sortate descrescător după dată
    List<Post> findAllByOrderByCreatedAtDesc();

    // Filtrare după hashtag
    @Query("SELECT p FROM Post p JOIN p.hashtags h WHERE h.name = :hashtag ORDER BY p.createdAt DESC")
    List<Post> findByHashtag(@Param("hashtag") String hashtag);

    // Filtrare după conținut (căutare în text)
    List<Post> findByContentContainingIgnoreCaseOrderByCreatedAtDesc(String keyword);

    // Filtrare după autor
    List<Post> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
}
