package com.studyplatform.repository;

import com.studyplatform.entity.WorkSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface WorkSessionRepository extends JpaRepository<WorkSession, UUID> {

    List<WorkSession> findByGroupIdOrderByScheduledAtDesc(UUID groupId);

    List<WorkSession> findByGroupIdAndScheduledAtAfterOrderByScheduledAtAsc(UUID groupId, Instant after);
}
