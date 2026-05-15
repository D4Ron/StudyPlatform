package com.studyplatform.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.studyplatform.ai.AiService;
import com.studyplatform.ai.DocumentExtractor;
import com.studyplatform.entity.Document;
import com.studyplatform.enums.DetailLevel;
import com.studyplatform.enums.Difficulty;
import com.studyplatform.enums.ExpertiseLevel;
import com.studyplatform.enums.QuizType;
import com.studyplatform.security.CurrentUser;
import com.studyplatform.security.UserPrincipal;
import com.studyplatform.service.DocumentService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Temporary controller for testing AI features in isolation.
 * Can be removed once GuideController and QuizController are built.
 */
@RestController
@RequestMapping("/api/ai/test")
@RequiredArgsConstructor
public class AiTestController {

    private final AiService aiService;
    private final DocumentExtractor documentExtractor;
    private final DocumentService documentService;

    @PostMapping("/guide")
    public ResponseEntity<JsonNode> testGuideGeneration(
            @CurrentUser UserPrincipal principal,
            @RequestBody GuideTestRequest request) {

        String storageKey = null;
        if (request.getDocumentId() != null) {
            Document doc = documentService.getDocumentEntity(request.getDocumentId());
            storageKey = doc.getStorageKey();
        }

        JsonNode guide = aiService.generateGuide(
                request.getTopic(),
                request.getLevel(),
                request.getSpecificConcept(),
                storageKey);

        return ResponseEntity.ok(guide);
    }

    @PostMapping("/quiz")
    public ResponseEntity<JsonNode> testQuizGeneration(
            @CurrentUser UserPrincipal principal,
            @RequestBody QuizTestRequest request) {

        JsonNode quiz = aiService.generateQuiz(
                request.getTopic(),
                request.getDifficulty(),
                request.getQuizType(),
                request.getQuestionCount(),
                null);

        return ResponseEntity.ok(quiz);
    }

    @PostMapping("/explain")
    public ResponseEntity<JsonNode> testExplanation(
            @CurrentUser UserPrincipal principal,
            @RequestBody ExplainTestRequest request) {

        JsonNode explanation = aiService.explainConcept(
                request.getConcept(),
                request.getLevel());

        return ResponseEntity.ok(explanation);
    }

    @PostMapping("/extract/{documentId}")
    public ResponseEntity<Map<String, Object>> testExtraction(
            @CurrentUser UserPrincipal principal,
            @PathVariable UUID documentId) {

        Document doc = documentService.getDocumentEntity(documentId);
        String text = documentExtractor.extractPreview(doc.getStorageKey(), 2000);

        return ResponseEntity.ok(Map.of(
                "documentId", documentId,
                "filename", doc.getFilename(),
                "extractedChars", text.length(),
                "preview", text
        ));
    }

    @Data
    static class GuideTestRequest {
        private String topic;
        private ExpertiseLevel level;
        private String specificConcept;
        private UUID documentId;
    }

    @Data
    static class QuizTestRequest {
        private String topic;
        private Difficulty difficulty;
        private QuizType quizType;
        private int questionCount = 5;
    }

    @Data
    static class ExplainTestRequest {
        private String concept;
        private DetailLevel level;
    }
}
