package com.studyplatform.dto.competition;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data @Builder
public class LeaderboardEntry {
    private int rank;
    private UUID userId;
    private String firstName;
    private String lastName;
    private int score;
    private int totalPoints;
    private int correctCount;
    private int totalQuestions;
    private double percentageScore;
    private int timeTakenSeconds;
    private boolean completed;
}
