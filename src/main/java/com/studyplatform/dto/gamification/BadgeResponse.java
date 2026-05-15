package com.studyplatform.dto.gamification;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class BadgeResponse {
    private UUID id;
    private String code;
    private String name;
    private String description;
    private String category;
    private int xpReward;
    private boolean earned;
    private Instant earnedAt;
    private Double progress;
}
