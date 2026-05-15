package com.studyplatform.repository;

import com.studyplatform.entity.StudySession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface StudySessionRepository extends JpaRepository<StudySession, UUID> {

    List<StudySession> findByUserIdOrderByStartTimeDesc(UUID userId);

    @Query("SELECT COALESCE(SUM(TIMESTAMPDIFF(SECOND, s.startTime, s.endTime)), 0) FROM StudySession s WHERE s.user.id = :userId AND s.endTime IS NOT NULL")
    long getTotalStudySecondsForUser(UUID userId);

    long countByUserId(UUID userId);

    List<StudySession> findByUserIdAndStartTimeAfterOrderByStartTimeDesc(UUID userId, Instant after);
}
