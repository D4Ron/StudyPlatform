package com.studyplatform.dto.guide;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class GuideResponse {
    private UUID id;
    private String title;
    private String topic;
    private String specificConcept;
    private String expertiseLevel;
    private boolean broadOverview;
    private JsonNode content;
    private int totalEstimatedMinutes;
    private UUID documentId;
    private Instant createdAt;
}
