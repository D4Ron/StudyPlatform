package com.studyplatform.dto.competition;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
public class CreateCompetitionRequest {
    @NotBlank private String title;
    private String description;
    @NotBlank private String topic;
    @NotNull private Instant startTime;
    @NotNull private Instant endTime;
    @NotBlank private String difficulty;
    private int questionCount = 15;
    private UUID groupId;
}
