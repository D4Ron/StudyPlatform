package com.studyplatform.service;

import com.studyplatform.dto.stats.DashboardStatsResponse;
import com.studyplatform.dto.stats.StudySessionRequest;
import com.studyplatform.entity.StudySession;
import com.studyplatform.entity.Topic;
import com.studyplatform.entity.User;
import com.studyplatform.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsService {

    private final XpLogRepository xpLogRepository;
    private final StudyGuideRepository guideRepository;
    private final QuizAttemptRepository attemptRepository;
    private final StudySessionRepository sessionRepository;
    private final TopicRepository topicRepository;

    public DashboardStatsResponse getDashboardStats(UUID userId) {
        int totalXp = xpLogRepository.getTotalXpByUserId(userId);
        long totalSeconds = sessionRepository.getTotalStudySecondsForUser(userId);
        long guidesCount = guideRepository.findByUserIdOrderByCreatedAtDesc(userId).size();
        long quizzesCount = attemptRepository.countByUserId(userId);
        Double avgScore = attemptRepository.findAverageScoreByUserId(userId);

        List<DashboardStatsResponse.TopicXp> xpByTopic = xpLogRepository.getXpByTopicForUser(userId)
                .stream()
                .map(row -> DashboardStatsResponse.TopicXp.builder()
                        .topic((String) row[0])
                        .xp(((Number) row[1]).longValue())
                        .build())
                .toList();

        List<DashboardStatsResponse.RecentActivity> recent = xpLogRepository
                .findByUserIdOrderByEarnedAtDesc(userId)
                .stream()
                .limit(10)
                .map(xp -> DashboardStatsResponse.RecentActivity.builder()
                        .type(xp.getSource())
                        .description(xp.getSource() + (xp.getTopic() != null ? " — " + xp.getTopic().getName() : ""))
                        .xpEarned(xp.getXpEarned())
                        .timestamp(xp.getEarnedAt().toString())
                        .build())
                .toList();

        return DashboardStatsResponse.builder()
                .totalXp(totalXp)
                .totalStudyMinutes(totalSeconds / 60)
                .guidesCompleted((int) guidesCount)
                .quizzesTaken((int) quizzesCount)
                .averageQuizScore(avgScore != null ? Math.round(avgScore * 10.0) / 10.0 : null)
                .xpByTopic(xpByTopic)
                .recentActivity(recent)
                .build();
    }

    @Transactional
    public void logSession(User user, StudySessionRequest request) {
        Topic topic = null;
        if (request.getTopicId() != null) {
            topic = topicRepository.findById(request.getTopicId()).orElse(null);
        }

        StudySession session = StudySession.builder()
                .user(user)
                .topic(topic)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .focusScore(request.getFocusScore())
                .activity(request.getActivity())
                .build();

        sessionRepository.save(session);
        log.info("Study session logged for user {}", user.getEmail());
    }
}
