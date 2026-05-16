package com.studyplatform.dto.quiz;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class QuizAttemptResponse {
    private UUID id;
    private UUID quizId;
    private String quizTitle;
    private int score;
    private int totalPoints;
    private int correctCount;
    private int totalQuestions;
    private double percentageScore;
    private JsonNode answers;
    private int timeTakenSeconds;
    private Instant completedAt;
}
