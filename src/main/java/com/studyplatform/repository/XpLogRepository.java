package com.studyplatform.repository;

import com.studyplatform.entity.XpLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface XpLogRepository extends JpaRepository<XpLog, UUID> {

    @Query("SELECT COALESCE(SUM(x.xpEarned), 0) FROM XpLog x WHERE x.user.id = :userId")
    int getTotalXpByUserId(UUID userId);

    @Query("SELECT x.topic.name, SUM(x.xpEarned) FROM XpLog x WHERE x.user.id = :userId AND x.topic IS NOT NULL GROUP BY x.topic.name ORDER BY SUM(x.xpEarned) DESC")
    List<Object[]> getXpByTopicForUser(UUID userId);

    List<XpLog> findByUserIdOrderByEarnedAtDesc(UUID userId);
}
