package com.studyplatform.dto.group;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class WorkSessionResponse {
    private UUID id;
    private String title;
    private Instant scheduledAt;
    private int durationMinutes;
    private boolean active;
    private String createdByName;
    private Instant createdAt;
}
