package com.studyplatform.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyplatform.ai.AiService;
import com.studyplatform.dto.recommendation.RecommendationResponse;
import com.studyplatform.dto.stats.DashboardStatsResponse;
import com.studyplatform.entity.Recommendation;
import com.studyplatform.entity.User;
import com.studyplatform.repository.QuizAttemptRepository;
import com.studyplatform.repository.RecommendationRepository;
import com.studyplatform.repository.XpLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final XpLogRepository xpLogRepository;
    private final QuizAttemptRepository attemptRepository;
    private final StatsService statsService;
    private final AiService aiService;
    private final ObjectMapper objectMapper;

    public List<RecommendationResponse> getRecommendations(UUID userId) {
        List<Recommendation> recs = recommendationRepository
                .findByUserIdOrderByCreatedAtDesc(userId);

        if (recs.isEmpty()) {
            return List.of();
        }

        return recs.stream()
                .limit(5)
                .map(this::toResponse)
                .toList();
    }

    @Async
    @Transactional
    public void generateRecommendations(User user) {
        try {
            log.info("Generating recommendations for user {}", user.getEmail());

            // Build learning profile from stats
            DashboardStatsResponse stats = statsService.getDashboardStats(user.getId());
            String profile = buildLearningProfile(user, stats);

            // Get AI recommendations
            JsonNode aiRecs = aiService.generateRecommendations(profile);

            // Clear old recommendations
            recommendationRepository.deleteByUserId(user.getId());

            // Save new ones
            if (aiRecs.has("recommendations")) {
                for (JsonNode rec : aiRecs.get("recommendations")) {
                    Recommendation entity = Recommendation.builder()
                            .user(user)
                            .title(rec.has("title") ? rec.get("title").asText() : "Study suggestion")
                            .description(rec.has("description") ? rec.get("description").asText() : "")
                            .reason(rec.has("reason") ? rec.get("reason").asText() : "NATURAL_PROGRESSION")
                            .relatedTopic(rec.has("relatedTopic") ? rec.get("relatedTopic").asText() : null)
                            .suggestedAction(rec.has("suggestedAction") ? rec.get("suggestedAction").asText() : "GENERATE_GUIDE")
                            .source("AI_GENERATED")
                            .status("PENDING")
                            .build();
                    recommendationRepository.save(entity);
                }
            }

            log.info("Generated {} recommendations for user {}",
                    aiRecs.has("recommendations") ? aiRecs.get("recommendations").size() : 0,
                    user.getEmail());

        } catch (Exception e) {
            log.error("Failed to generate recommendations for {}: {}", user.getEmail(), e.getMessage());
        }
    }

    @Transactional
    public void markActedOn(UUID recommendationId, UUID userId) {
        Recommendation rec = recommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new RuntimeException("Recommendation not found"));
        if (!rec.getUser().getId().equals(userId)) return;
        rec.setStatus("ACTED_ON");
        recommendationRepository.save(rec);
    }

    private String buildLearningProfile(User user, DashboardStatsResponse stats) {
        StringBuilder sb = new StringBuilder();
        sb.append("Student profile:\n");
        sb.append("- Total XP: ").append(stats.getTotalXp()).append("\n");
        sb.append("- Guides completed: ").append(stats.getGuidesCompleted()).append("\n");
        sb.append("- Quizzes taken: ").append(stats.getQuizzesTaken()).append("\n");

        if (stats.getAverageQuizScore() != null) {
            sb.append("- Average quiz score: ").append(stats.getAverageQuizScore()).append("%\n");
        }

        if (!stats.getXpByTopic().isEmpty()) {
            sb.append("- XP by topic:\n");
            for (var topicXp : stats.getXpByTopic()) {
                sb.append("  - ").append(topicXp.getTopic()).append(": ").append(topicXp.getXp()).append(" XP\n");
            }
        }

        if (user.getPreferenceDomains() != null) {
            sb.append("- Preference domains: ").append(user.getPreferenceDomains()).append("\n");
        }
        if (user.getObjectives() != null) {
            sb.append("- Learning objectives: ").append(user.getObjectives()).append("\n");
        }

        return sb.toString();
    }

    private RecommendationResponse toResponse(Recommendation rec) {
        return RecommendationResponse.builder()
                .id(rec.getId())
                .title(rec.getTitle())
                .description(rec.getDescription())
                .reason(rec.getReason())
                .relatedTopic(rec.getRelatedTopic())
                .suggestedAction(rec.getSuggestedAction())
                .source(rec.getSource())
                .status(rec.getStatus())
                .createdAt(rec.getCreatedAt())
                .build();
    }
}
