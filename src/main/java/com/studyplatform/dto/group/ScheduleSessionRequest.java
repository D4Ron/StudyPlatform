package com.studyplatform.dto.group;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

@Data
public class ScheduleSessionRequest {

    private String title;

    @NotNull(message = "Scheduled time is required")
    @Future(message = "Session must be scheduled in the future")
    private Instant scheduledAt;

    @Min(value = 15, message = "Session must be at least 15 minutes")
    private int durationMinutes;
}
