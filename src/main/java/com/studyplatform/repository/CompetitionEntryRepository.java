package com.studyplatform.repository;

import com.studyplatform.entity.CompetitionEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompetitionEntryRepository extends JpaRepository<CompetitionEntry, UUID> {
    List<CompetitionEntry> findByCompetitionIdOrderByScoreDesc(UUID competitionId);
    List<CompetitionEntry> findByCompetitionIdAndCompletedTrueOrderByScoreDesc(UUID competitionId);
    Optional<CompetitionEntry> findByCompetitionIdAndUserId(UUID competitionId, UUID userId);
    boolean existsByCompetitionIdAndUserId(UUID competitionId, UUID userId);
    long countByCompetitionId(UUID competitionId);
}
