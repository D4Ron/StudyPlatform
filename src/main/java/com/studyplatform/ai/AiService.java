package com.studyplatform.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyplatform.enums.DetailLevel;
import com.studyplatform.enums.Difficulty;
import com.studyplatform.enums.ExpertiseLevel;
import com.studyplatform.enums.QuizType;
import com.studyplatform.exception.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

    private final ClaudeClient claudeClient;
    private final PromptBuilder promptBuilder;
    private final DocumentExtractor documentExtractor;
    private final ObjectMapper objectMapper;

    // ── Study Guide ───────────────────────────────────────────

    public JsonNode generateGuide(String topic, ExpertiseLevel level,
                                   String specificConcept, String documentStorageKey) {
        String documentText = null;
        if (documentStorageKey != null) {
            documentText = documentExtractor.extractText(documentStorageKey);
        }

        String userMessage = promptBuilder.buildGuideUserMessage(
                topic, level, specificConcept, documentText);

        String response = claudeClient.chat(
                PromptBuilder.GUIDE_SYSTEM_PROMPT, userMessage, 16000);

        return parseJson(response, "guide");
    }

    // ── Quiz ──────────────────────────────────────────────────

    public JsonNode generateQuiz(String topic, Difficulty difficulty,
                                  QuizType quizType, int questionCount,
                                  String guideContent) {
        String userMessage = promptBuilder.buildQuizUserMessage(
                topic, difficulty, quizType, questionCount, guideContent);

        String response = claudeClient.chat(
                PromptBuilder.QUIZ_SYSTEM_PROMPT, userMessage, 8000);

        return parseJson(response, "quiz");
    }

    // ── Concept Explanation ───────────────────────────────────

    public JsonNode explainConcept(String concept, DetailLevel level) {
        String userMessage = promptBuilder.buildExplanationUserMessage(concept, level);

        String response = claudeClient.chat(
                PromptBuilder.EXPLANATION_SYSTEM_PROMPT, userMessage, 4000);

        return parseJson(response, "explanation");
    }

    // ── Recommendations ───────────────────────────────────────

    public JsonNode generateRecommendations(String learningProfile) {
        String userMessage = promptBuilder.buildRecommendationUserMessage(learningProfile);

        String response = claudeClient.chat(
                PromptBuilder.RECOMMENDATION_SYSTEM_PROMPT, userMessage, 2000);

        return parseJson(response, "recommendations");
    }

    // ── Document Summary (async — called after upload) ────────

    @Async
    public CompletableFuture<JsonNode> summarizeDocument(String storageKey) {
        try {
            String documentText = documentExtractor.extractPreview(storageKey, 20000);
            String userMessage = promptBuilder.buildSummaryUserMessage(documentText);

            String response = claudeClient.chat(
                    PromptBuilder.SUMMARY_SYSTEM_PROMPT, userMessage, 1500);

            return CompletableFuture.completedFuture(parseJson(response, "summary"));
        } catch (Exception e) {
            log.error("Async document summary failed for {}: {}", storageKey, e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    // ── JSON Parsing ──────────────────────────────────────────

    private JsonNode parseJson(String response, String context) {
        // Claude sometimes wraps JSON in markdown code fences
        String cleaned = response.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        cleaned = cleaned.trim();

        try {
            return objectMapper.readTree(cleaned);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse Claude {} response as JSON: {}", context, e.getMessage());
            log.debug("Raw response: {}", response);
            throw ApiException.badRequest(
                    "AI returned an invalid response. Please try again.");
        }
    }
}
