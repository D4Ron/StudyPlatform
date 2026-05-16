package com.studyplatform.dto.guide;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class GuideListResponse {
    private UUID id;
    private String title;
    private String topic;
    private String expertiseLevel;
    private int totalEstimatedMinutes;
    private int moduleCount;
    private Instant createdAt;
}
