package com.studyplatform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "competition_entries", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"competition_id", "user_id"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CompetitionEntry {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competition_id", nullable = false) private Competition competition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) private User user;

    private int score;
    private int totalPoints;
    private int correctCount;
    private int totalQuestions;
    private int timeTakenSeconds;
    private boolean completed;

    @CreationTimestamp @Column(updatable = false) private Instant joinedAt;
    private Instant completedAt;
}
