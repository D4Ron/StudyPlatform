package com.studyplatform.dto.stats;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DashboardStatsResponse {
    private int totalXp;
    private long totalStudyMinutes;
    private int guidesCompleted;
    private int quizzesTaken;
    private Double averageQuizScore;
    private List<TopicXp> xpByTopic;
    private List<RecentActivity> recentActivity;

    @Data
    @Builder
    public static class TopicXp {
        private String topic;
        private long xp;
    }

    @Data
    @Builder
    public static class RecentActivity {
        private String type;
        private String description;
        private int xpEarned;
        private String timestamp;
    }
}
