package com.studyplatform.dto.topic;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class TopicResponse {
    private UUID id;
    private String name;
    private String specificity;
    private Instant createdAt;
}
