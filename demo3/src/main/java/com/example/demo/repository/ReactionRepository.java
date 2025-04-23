package com.example.demo.repository;

import com.example.demo.entity.Reaction;
import com.example.demo.enumeration.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, Long> {

    Optional<Reaction> findByUserIdAndTargetIdAndTargetType(Long userId, Long targetId, TargetType targetType);

    List<Reaction> findByTargetIdAndTargetType(Long targetId, TargetType targetType);

    @Query("SELECT r.type, COUNT(r) FROM Reaction r WHERE r.targetId = :targetId AND r.targetType = :targetType GROUP BY r.type")
    List<Object[]> countReactionsByType(@Param("targetId") Long targetId, @Param("targetType") TargetType targetType);
}
