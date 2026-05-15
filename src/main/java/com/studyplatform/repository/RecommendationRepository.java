package com.studyplatform.repository;

import com.studyplatform.entity.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, UUID> {
    List<Recommendation> findByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, String status);
    List<Recommendation> findByUserIdOrderByCreatedAtDesc(UUID userId);
    void deleteByUserId(UUID userId);
}
