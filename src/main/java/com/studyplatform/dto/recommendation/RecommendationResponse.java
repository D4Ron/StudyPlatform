package com.studyplatform.dto.recommendation;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class RecommendationResponse {
    private UUID id;
    private String title;
    private String description;
    private String reason;
    private String relatedTopic;
    private String suggestedAction;
    private String source;
    private String status;
    private Instant createdAt;
}
