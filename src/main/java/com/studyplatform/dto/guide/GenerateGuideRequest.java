package com.studyplatform.dto.guide;

import com.studyplatform.enums.ExpertiseLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class GenerateGuideRequest {

    @NotBlank(message = "Topic is required")
    private String topic;

    @NotNull(message = "Expertise level is required")
    private ExpertiseLevel expertiseLevel;

    // If null, generates a broad overview
    private String specificConcept;

    // Optional — generate guide from this document's content
    private UUID documentId;

    // Optional — link to an existing topic entity
    private UUID topicId;
}
