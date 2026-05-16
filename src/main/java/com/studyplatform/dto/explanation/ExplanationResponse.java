package com.studyplatform.dto.explanation;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ExplanationResponse {
    private UUID id;
    private String concept;
    private String detailLevel;
    private JsonNode content;
    private Instant createdAt;
}
