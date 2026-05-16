package com.studyplatform.service;

import com.studyplatform.dto.gamification.BadgeResponse;
import com.studyplatform.dto.gamification.LevelResponse;
import com.studyplatform.entity.Badge;
import com.studyplatform.entity.User;
import com.studyplatform.entity.UserBadge;
import com.studyplatform.repository.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final XpLogRepository xpLogRepository;
    private final StudyGuideRepository guideRepository;
    private final QuizAttemptRepository attemptRepository;

    // ── Level system ──────────────────────────────────────────

    private static final int[] LEVEL_THRESHOLDS = {
            0, 50, 150, 300, 500, 800, 1200, 1700, 2300, 3000,
            4000, 5000, 6500, 8000, 10000, 12500, 15000, 18000, 22000, 27000
    };

    private static final String[] LEVEL_TITLES = {
            "Newcomer", "Apprentice", "Student", "Learner", "Scholar",
            "Dedicated", "Studious", "Knowledgeable", "Expert", "Master",
            "Guru", "Sage", "Virtuoso", "Luminary", "Prodigy",
            "Savant", "Genius", "Visionary", "Legend", "Transcendent"
    };

    public LevelResponse getLevel(UUID userId) {
        int totalXp = xpLogRepository.getTotalXpByUserId(userId);
        int level = 0;

        for (int i = LEVEL_THRESHOLDS.length - 1; i >= 0; i--) {
            if (totalXp >= LEVEL_THRESHOLDS[i]) {
                level = i;
                break;
            }
        }

        int currentThreshold = LEVEL_THRESHOLDS[level];
        int nextThreshold = level < LEVEL_THRESHOLDS.length - 1
                ? LEVEL_THRESHOLDS[level + 1]
                : LEVEL_THRESHOLDS[level] + 5000;

        int xpInLevel = totalXp - currentThreshold;
        int xpNeeded = nextThreshold - currentThreshold;
        double progress = xpNeeded > 0 ? (xpInLevel * 100.0 / xpNeeded) : 100.0;

        return LevelResponse.builder()
                .level(level + 1)
                .title(level < LEVEL_TITLES.length ? LEVEL_TITLES[level] : "Transcendent")
                .currentXp(totalXp)
                .xpForCurrentLevel(currentThreshold)
                .xpForNextLevel(nextThreshold)
                .progressPercent(Math.round(progress * 10.0) / 10.0)
                .build();
    }

    // ── Badges ────────────────────────────────────────────────

    public List<BadgeResponse> getAllBadgesForUser(UUID userId) {
        List<Badge> allBadges = badgeRepository.findAll();
        return allBadges.stream().map(badge -> {
            boolean earned = userBadgeRepository.existsByUserIdAndBadgeId(userId, badge.getId());
            UserBadge ub = earned ? userBadgeRepository.findByUserIdOrderByEarnedAtDesc(userId)
                    .stream().filter(u -> u.getBadge().getId().equals(badge.getId()))
                    .findFirst().orElse(null) : null;

            double progress = calculateProgress(userId, badge);

            return BadgeResponse.builder()
                    .id(badge.getId())
                    .code(badge.getCode())
                    .name(badge.getName())
                    .description(badge.getDescription())
                    .category(badge.getCategory())
                    .xpReward(badge.getXpReward())
                    .earned(earned)
                    .earnedAt(ub != null ? ub.getEarnedAt() : null)
                    .progress(earned ? 100.0 : progress)
                    .build();
        }).toList();
    }

    @Transactional
    public void checkAndAwardBadges(User user) {
        List<Badge> allBadges = badgeRepository.findAll();
        int totalXp = xpLogRepository.getTotalXpByUserId(user.getId());
        long guideCount = guideRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).size();
        long quizCount = attemptRepository.countByUserId(user.getId());

        for (Badge badge : allBadges) {
            if (userBadgeRepository.existsByUserIdAndBadgeId(user.getId(), badge.getId())) {
                continue;
            }

            boolean earned = switch (badge.getCondition()) {
                case "TOTAL_XP" -> totalXp >= badge.getThreshold();
                case "GUIDES_COMPLETED" -> guideCount >= badge.getThreshold();
                case "QUIZZES_TAKEN" -> quizCount >= badge.getThreshold();
                case "FIRST_GUIDE" -> guideCount >= 1;
                case "FIRST_QUIZ" -> quizCount >= 1;
                default -> false;
            };

            if (earned) {
                UserBadge ub = UserBadge.builder()
                        .user(user)
                        .badge(badge)
                        .build();
                userBadgeRepository.save(ub);
                log.info("Badge '{}' awarded to user {}", badge.getName(), user.getEmail());
            }
        }
    }

    private double calculateProgress(UUID userId, Badge badge) {
        long current = switch (badge.getCondition()) {
            case "TOTAL_XP" -> xpLogRepository.getTotalXpByUserId(userId);
            case "GUIDES_COMPLETED" -> guideRepository.findByUserIdOrderByCreatedAtDesc(userId).size();
            case "QUIZZES_TAKEN" -> attemptRepository.countByUserId(userId);
            case "FIRST_GUIDE" -> guideRepository.findByUserIdOrderByCreatedAtDesc(userId).size();
            case "FIRST_QUIZ" -> attemptRepository.countByUserId(userId);
            default -> 0;
        };

        if (badge.getThreshold() <= 0) return current > 0 ? 100.0 : 0.0;
        return Math.min(100.0, Math.round(current * 1000.0 / badge.getThreshold()) / 10.0);
    }

    // ── Seed default badges on startup ────────────────────────

    @PostConstruct
    @Transactional
    public void seedBadges() {
        if (badgeRepository.count() > 0) return;

        List<Badge> defaults = List.of(
                Badge.builder().code("NEW_BEGINNING").name("New Beginning").description("Welcome to the platform").category("MILESTONE").condition("FIRST_GUIDE").threshold(1).xpReward(10).build(),
                Badge.builder().code("FIRST_QUIZ").name("Quiz Taker").description("Complete your first quiz").category("MILESTONE").condition("FIRST_QUIZ").threshold(1).xpReward(10).build(),
                Badge.builder().code("GUIDE_5").name("Knowledge Seeker").description("Generate 5 study guides").category("GUIDES").condition("GUIDES_COMPLETED").threshold(5).xpReward(25).build(),
                Badge.builder().code("GUIDE_10").name("Study Marathon").description("Generate 10 study guides").category("GUIDES").condition("GUIDES_COMPLETED").threshold(10).xpReward(50).build(),
                Badge.builder().code("QUIZ_5").name("Test Warrior").description("Complete 5 quizzes").category("QUIZZES").condition("QUIZZES_TAKEN").threshold(5).xpReward(25).build(),
                Badge.builder().code("QUIZ_10").name("Exam Ready").description("Complete 10 quizzes").category("QUIZZES").condition("QUIZZES_TAKEN").threshold(10).xpReward(50).build(),
                Badge.builder().code("XP_500").name("Rising Star").description("Earn 500 XP").category("XP").condition("TOTAL_XP").threshold(500).xpReward(30).build(),
                Badge.builder().code("XP_1000").name("Dedicated Learner").description("Earn 1000 XP").category("XP").condition("TOTAL_XP").threshold(1000).xpReward(50).build(),
                Badge.builder().code("XP_5000").name("Knowledge Master").description("Earn 5000 XP").category("XP").condition("TOTAL_XP").threshold(5000).xpReward(100).build()
        );

        badgeRepository.saveAll(defaults);
        log.info("Seeded {} default badges", defaults.size());
    }
}
