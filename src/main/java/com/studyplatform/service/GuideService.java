package com.studyplatform.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyplatform.ai.AiService;
import com.studyplatform.dto.guide.GenerateGuideRequest;
import com.studyplatform.dto.guide.GuideListResponse;
import com.studyplatform.dto.guide.GuideResponse;
import com.studyplatform.entity.Document;
import com.studyplatform.entity.StudyGuide;
import com.studyplatform.entity.Topic;
import com.studyplatform.entity.User;
import com.studyplatform.exception.ApiException;
import com.studyplatform.repository.DocumentRepository;
import com.studyplatform.repository.StudyGuideRepository;
import com.studyplatform.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GuideService {

    private final StudyGuideRepository guideRepository;
    private final TopicRepository topicRepository;
    private final DocumentRepository documentRepository;
    private final AiService aiService;
    private final ObjectMapper objectMapper;
    private final XpService xpService;
    private final BadgeService badgeService;

    @Transactional
    public GuideResponse generate(User user, GenerateGuideRequest request) {
        Topic topic = null;
        if (request.getTopicId() != null) {
            topic = topicRepository.findById(request.getTopicId())
                    .orElseThrow(() -> ApiException.notFound("Topic not found"));
        }

        Document document = null;
        String storageKey = null;
        if (request.getDocumentId() != null) {
            document = documentRepository.findById(request.getDocumentId())
                    .orElseThrow(() -> ApiException.notFound("Document not found"));
            storageKey = document.getStorageKey();
        }

        boolean broad = request.getSpecificConcept() == null || request.getSpecificConcept().isBlank();

        log.info("Generating guide for user {} — topic: {}, level: {}, broad: {}",
                user.getEmail(), request.getTopic(), request.getExpertiseLevel(), broad);

        JsonNode guideJson = aiService.generateGuide(
                request.getTopic(),
                request.getExpertiseLevel(),
                request.getSpecificConcept(),
                storageKey);

        String title = guideJson.has("title")
                ? guideJson.get("title").asText()
                : request.getTopic();

        int estimatedMinutes = guideJson.has("totalEstimatedMinutes")
                ? guideJson.get("totalEstimatedMinutes").asInt()
                : 60;

        StudyGuide guide = StudyGuide.builder()
                .user(user)
                .topic(topic)
                .document(document)
                .title(title)
                .specificConcept(request.getSpecificConcept())
                .expertiseLevel(request.getExpertiseLevel())
                .broadOverview(broad)
                .content(guideJson.toString())
                .totalEstimatedMinutes(estimatedMinutes)
                .build();

        guide = guideRepository.save(guide);
        log.info("Guide saved: {} ({})", guide.getTitle(), guide.getId());

        xpService.awardXp(user,
                request.getTopicId(),
                XpService.XP_GUIDE_COMPLETED,
                "GUIDE_COMPLETED",
                guide.getId().toString());
        badgeService.checkAndAwardBadges(user);

        return toFullResponse(guide);
    }

    public List<GuideListResponse> listByUser(UUID userId) {
        return guideRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toListResponse)
                .toList();
    }

    public GuideResponse getById(UUID guideId, UUID userId) {
        StudyGuide guide = guideRepository.findById(guideId)
                .orElseThrow(() -> ApiException.notFound("Guide not found"));

        if (!guide.getUser().getId().equals(userId)) {
            throw ApiException.forbidden("You do not have access to this guide");
        }

        return toFullResponse(guide);
    }

    public StudyGuide getEntity(UUID guideId) {
        return guideRepository.findById(guideId)
                .orElseThrow(() -> ApiException.notFound("Guide not found"));
    }

    @Transactional
    public void delete(UUID guideId, UUID userId) {
        StudyGuide guide = guideRepository.findById(guideId)
                .orElseThrow(() -> ApiException.notFound("Guide not found"));

        if (!guide.getUser().getId().equals(userId)) {
            throw ApiException.forbidden("You do not have access to this guide");
        }

        guideRepository.delete(guide);
        log.info("Guide deleted: {}", guideId);
    }

    private GuideResponse toFullResponse(StudyGuide guide) {
        JsonNode contentNode;
        try {
            contentNode = objectMapper.readTree(guide.getContent());
        } catch (Exception e) {
            contentNode = objectMapper.createObjectNode();
        }

        return GuideResponse.builder()
                .id(guide.getId())
                .title(guide.getTitle())
                .topic(guide.getTopic() != null ? guide.getTopic().getName() : null)
                .specificConcept(guide.getSpecificConcept())
                .expertiseLevel(guide.getExpertiseLevel().name())
                .broadOverview(guide.isBroadOverview())
                .content(contentNode)
                .totalEstimatedMinutes(guide.getTotalEstimatedMinutes())
                .documentId(guide.getDocument() != null ? guide.getDocument().getId() : null)
                .createdAt(guide.getCreatedAt())
                .build();
    }

    private GuideListResponse toListResponse(StudyGuide guide) {
        int moduleCount = 0;
        try {
            JsonNode content = objectMapper.readTree(guide.getContent());
            if (content.has("modules")) {
                moduleCount = content.get("modules").size();
            }
        } catch (Exception ignored) {}

        return GuideListResponse.builder()
                .id(guide.getId())
                .title(guide.getTitle())
                .topic(guide.getTopic() != null ? guide.getTopic().getName() : null)
                .expertiseLevel(guide.getExpertiseLevel().name())
                .totalEstimatedMinutes(guide.getTotalEstimatedMinutes())
                .moduleCount(moduleCount)
                .createdAt(guide.getCreatedAt())
                .build();
    }
}
