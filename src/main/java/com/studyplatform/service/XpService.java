package com.studyplatform.service;

import com.studyplatform.entity.Topic;
import com.studyplatform.entity.User;
import com.studyplatform.entity.XpLog;
import com.studyplatform.repository.TopicRepository;
import com.studyplatform.repository.XpLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class XpService {

    private final XpLogRepository xpLogRepository;
    private final TopicRepository topicRepository;

    // XP amounts per activity
    public static final int XP_GUIDE_COMPLETED = 50;
    public static final int XP_QUIZ_COMPLETED = 30;
    public static final int XP_QUIZ_PERFECT = 20;  // bonus
    public static final int XP_EXPLANATION_VIEWED = 10;
    public static final int XP_DOCUMENT_UPLOADED = 5;

    @Transactional
    public void awardXp(User user, UUID topicId, int amount, String source, String sourceId) {
        Topic topic = null;
        if (topicId != null) {
            topic = topicRepository.findById(topicId).orElse(null);
        }

        XpLog xpLog = XpLog.builder()
                .user(user)
                .topic(topic)
                .xpEarned(amount)
                .source(source)
                .sourceId(sourceId)
                .build();

        xpLogRepository.save(xpLog);
        log.info("Awarded {} XP to user {} for {} ({})",
                amount, user.getEmail(), source, sourceId);
    }

    public int getTotalXp(UUID userId) {
        return xpLogRepository.getTotalXpByUserId(userId);
    }
}
