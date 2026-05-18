package com.studyplatform.repository;

import com.studyplatform.entity.Competition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface CompetitionRepository extends JpaRepository<Competition, UUID> {
    List<Competition> findByGroupIdOrderByStartTimeDesc(UUID groupId);
    List<Competition> findByActiveTrueOrderByStartTimeDesc();

    @Query("SELECT c FROM Competition c WHERE c.endTime > :now AND c.active = true ORDER BY c.startTime ASC")
    List<Competition> findUpcoming(Instant now);

    @Query("SELECT c FROM Competition c WHERE c.startTime <= :now AND c.endTime >= :now AND c.active = true")
    List<Competition> findOngoing(Instant now);
}
