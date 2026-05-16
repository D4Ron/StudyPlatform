package com.studyplatform.dto.stats;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class StudySessionRequest {

    @NotNull(message = "Start time is required")
    private Instant startTime;

    private Instant endTime;

    private UUID topicId;

    private int focusScore;

    private String activity;
}
