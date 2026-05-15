package com.studyplatform.dto.quiz;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class QuizResponse {
    private UUID id;
    private String title;
    private String topic;
    private String difficulty;
    private String quizType;
    private int questionCount;
    private JsonNode questions;
    private UUID guideId;
    private Instant createdAt;
}
