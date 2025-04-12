package com.example.demo.repository;

import com.example.demo.dto.hashtagdto.HashtagDTO;
import com.example.demo.entity.Hashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface HashtagRepository extends JpaRepository<Hashtag, Long> {
    Optional<Hashtag> findByName(String name);
    boolean existsByName(String name);
}
