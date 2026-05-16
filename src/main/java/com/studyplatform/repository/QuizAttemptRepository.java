package com.studyplatform.repository;

import com.studyplatform.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, UUID> {
    List<QuizAttempt> findByUserIdOrderByCompletedAtDesc(UUID userId);
    List<QuizAttempt> findByQuizIdOrderByCompletedAtDesc(UUID quizId);

    @Query("SELECT AVG(a.score * 100.0 / a.totalPoints) FROM QuizAttempt a WHERE a.user.id = :userId AND a.totalPoints > 0")
    Double findAverageScoreByUserId(UUID userId);

    long countByUserId(UUID userId);
}
