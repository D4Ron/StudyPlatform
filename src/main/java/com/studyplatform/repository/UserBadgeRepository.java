package com.studyplatform.repository;

import com.studyplatform.entity.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, UUID> {
    List<UserBadge> findByUserIdOrderByEarnedAtDesc(UUID userId);
    boolean existsByUserIdAndBadgeId(UUID userId, UUID badgeId);
    boolean existsByUserIdAndBadgeCode(UUID userId, String badgeCode);
    long countByUserId(UUID userId);
}
