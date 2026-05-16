package com.studyplatform.dto.gamification;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LevelResponse {
    private int level;
    private String title;
    private int currentXp;
    private int xpForCurrentLevel;
    private int xpForNextLevel;
    private double progressPercent;
}
