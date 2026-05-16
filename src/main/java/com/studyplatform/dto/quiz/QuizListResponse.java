package com.studyplatform.dto.quiz;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class QuizListResponse {
    private UUID id;
    private String title;
    private String topic;
    private String difficulty;
    private String quizType;
    private int questionCount;
    private Instant createdAt;
}
