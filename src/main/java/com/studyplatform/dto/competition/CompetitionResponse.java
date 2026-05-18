package com.studyplatform.dto.competition;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data @Builder
public class CompetitionResponse {
    private UUID id;
    private String title;
    private String description;
    private String topic;
    private String difficulty;
    private int questionCount;
    private Instant startTime;
    private Instant endTime;
    private boolean active;
    private boolean ongoing;
    private long participantCount;
    private String createdByName;
    private UUID groupId;
    private String groupName;
    private JsonNode questions; // null until competition starts
    private Boolean alreadyJoined;
    private Instant createdAt;
}
