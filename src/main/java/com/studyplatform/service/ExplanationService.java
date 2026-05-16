package com.studyplatform.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyplatform.ai.AiService;
import com.studyplatform.dto.explanation.ExplainConceptRequest;
import com.studyplatform.dto.explanation.ExplanationResponse;
import com.studyplatform.entity.ConceptExplanation;
import com.studyplatform.entity.User;
import com.studyplatform.exception.ApiException;
import com.studyplatform.repository.ConceptExplanationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExplanationService {

    private final ConceptExplanationRepository explanationRepository;
    private final AiService aiService;
    private final ObjectMapper objectMapper;
    private final XpService xpService;

    @Transactional
    public ExplanationResponse explain(User user, ExplainConceptRequest request) {
        log.info("Generating explanation for user {} — concept: {}, level: {}",
                user.getEmail(), request.getConcept(), request.getDetailLevel());

        JsonNode explanationJson = aiService.explainConcept(
                request.getConcept(), request.getDetailLevel());

        ConceptExplanation explanation = ConceptExplanation.builder()
                .user(user)
                .concept(request.getConcept())
                .detailLevel(request.getDetailLevel())
                .content(explanationJson.toString())
                .build();

        explanation = explanationRepository.save(explanation);
        xpService.awardXp(user, null,
                XpService.XP_EXPLANATION_VIEWED,
                "EXPLANATION_VIEWED",
                explanation.getId().toString());
        return toResponse(explanation);
    }

    public List<ExplanationResponse> listByUser(UUID userId) {
        return explanationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    public ExplanationResponse getById(UUID explanationId, UUID userId) {
        ConceptExplanation exp = explanationRepository.findById(explanationId)
                .orElseThrow(() -> ApiException.notFound("Explanation not found"));

        if (!exp.getUser().getId().equals(userId)) {
            throw ApiException.forbidden("You do not have access to this explanation");
        }
        return toResponse(exp);
    }

    @Transactional
    public void delete(UUID explanationId, UUID userId) {
        ConceptExplanation exp = explanationRepository.findById(explanationId)
                .orElseThrow(() -> ApiException.notFound("Explanation not found"));

        if (!exp.getUser().getId().equals(userId)) {
            throw ApiException.forbidden("You do not have access to this explanation");
        }
        explanationRepository.delete(exp);
    }

    private ExplanationResponse toResponse(ConceptExplanation exp) {
        JsonNode contentNode;
        try {
            contentNode = objectMapper.readTree(exp.getContent());
        } catch (Exception e) {
            contentNode = objectMapper.createObjectNode();
        }

        return ExplanationResponse.builder()
                .id(exp.getId())
                .concept(exp.getConcept())
                .detailLevel(exp.getDetailLevel().name())
                .content(contentNode)
                .createdAt(exp.getCreatedAt())
                .build();
    }
}
