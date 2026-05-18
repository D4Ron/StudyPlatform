package com.studyplatform.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        return parseJsonSafe(response, "guide");
    }

    // ── Quiz ──────────────────────────────────────────────────

    public JsonNode generateQuiz(String topic, Difficulty difficulty,
                                  QuizType quizType, int questionCount,
                                  String guideContent) {
        String userMessage = promptBuilder.buildQuizUserMessage(
                topic, difficulty, quizType, questionCount, guideContent);

        String response = claudeClient.chat(
                PromptBuilder.QUIZ_SYSTEM_PROMPT, userMessage, 16000);

        return parseJsonSafe(response, "quiz");
    }

    // ── Concept Explanation ───────────────────────────────────

    public JsonNode explainConcept(String concept, DetailLevel level) {
        String userMessage = promptBuilder.buildExplanationUserMessage(concept, level);

        String response = claudeClient.chat(
                PromptBuilder.EXPLANATION_SYSTEM_PROMPT, userMessage, 8000);

        return parseJsonSafe(response, "explanation");
    }

    // ── Recommendations ───────────────────────────────────────

    public JsonNode generateRecommendations(String learningProfile) {
        String userMessage = promptBuilder.buildRecommendationUserMessage(learningProfile);

        String response = claudeClient.chat(
                PromptBuilder.RECOMMENDATION_SYSTEM_PROMPT, userMessage, 4000);

        return parseJsonSafe(response, "recommendations");
    }

    // ── Document Summary (async) ──────────────────────────────

    @Async
    public CompletableFuture<JsonNode> summarizeDocument(String storageKey) {
        try {
            String documentText = documentExtractor.extractPreview(storageKey, 20000);
            String userMessage = promptBuilder.buildSummaryUserMessage(documentText);

            String response = claudeClient.chat(
                    PromptBuilder.SUMMARY_SYSTEM_PROMPT, userMessage, 2000);

            return CompletableFuture.completedFuture(parseJsonSafe(response, "summary"));
        } catch (Exception e) {
            log.error("Async document summary failed for {}: {}", storageKey, e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    // ══════════════════════════════════════════════════════════
    // ROBUST JSON PARSING — handles truncated, malformed, and
    // markdown-wrapped responses from Claude
    // ══════════════════════════════════════════════════════════

    private JsonNode parseJsonSafe(String response, String context) {
        String cleaned = stripMarkdownFences(response);

        // Attempt 1: direct parse
        try {
            return objectMapper.readTree(cleaned);
        } catch (JsonProcessingException e) {
            log.warn("Direct JSON parse failed for {} — attempting repair", context);
        }

        // Attempt 2: fix truncated JSON by closing open structures
        String repaired = repairTruncatedJson(cleaned);
        try {
            return objectMapper.readTree(repaired);
        } catch (JsonProcessingException e) {
            log.warn("Repaired JSON parse failed for {} — attempting extraction", context);
        }

        // Attempt 3: extract the largest valid JSON object from the response
        JsonNode extracted = extractLargestJson(cleaned);
        if (extracted != null) {
            log.info("Extracted partial JSON for {} — {} fields", context,
                    extracted.isObject() ? extracted.size() : "array");
            return extracted;
        }

        // Attempt 4: build a minimal valid structure
        log.error("All JSON parse attempts failed for {}. Building fallback.", context);
        return buildFallback(context, cleaned);
    }

    private String stripMarkdownFences(String response) {
        String s = response.trim();
        if (s.startsWith("```json")) s = s.substring(7);
        else if (s.startsWith("```")) s = s.substring(3);
        if (s.endsWith("```")) s = s.substring(0, s.length() - 3);
        return s.trim();
    }

    /**
     * Repair truncated JSON by counting open/close braces and brackets
     * and appending the necessary closing characters.
     */
    private String repairTruncatedJson(String json) {
        StringBuilder sb = new StringBuilder(json);
        boolean inString = false;
        char prev = 0;
        int braces = 0, brackets = 0;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"' && prev != '\\') {
                inString = !inString;
            }
            if (!inString) {
                if (c == '{') braces++;
                else if (c == '}') braces--;
                else if (c == '[') brackets++;
                else if (c == ']') brackets--;
            }
            prev = c;
        }

        // If we're inside a string, close it
        if (inString) {
            sb.append('"');
        }

        // Close any unclosed brackets/braces
        // Check if last meaningful char needs a value
        String trimmed = sb.toString().trim();
        if (trimmed.endsWith(",")) {
            sb = new StringBuilder(trimmed.substring(0, trimmed.length() - 1));
        } else if (trimmed.endsWith(":")) {
            sb.append("\"\"");
        }

        for (int i = 0; i < brackets; i++) sb.append(']');
        for (int i = 0; i < braces; i++) sb.append('}');

        return sb.toString();
    }

    /**
     * Try to find and parse the largest valid JSON object in the response.
     * Useful when Claude prepends/appends text around the JSON.
     */
    private JsonNode extractLargestJson(String text) {
        // Find the first { and try progressively shorter substrings
        int start = text.indexOf('{');
        if (start == -1) start = text.indexOf('[');
        if (start == -1) return null;

        String sub = text.substring(start);
        String repaired = repairTruncatedJson(sub);
        try {
            return objectMapper.readTree(repaired);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * Build a minimal valid fallback JSON so the app doesn't crash.
     */
    private JsonNode buildFallback(String context, String rawText) {
        ObjectNode fallback = objectMapper.createObjectNode();

        switch (context) {
            case "guide" -> {
                fallback.put("title", "Study Guide (generation incomplete)");
                ArrayNode modules = fallback.putArray("modules");
                ObjectNode mod = modules.addObject();
                mod.put("title", "Content");
                mod.put("type", "INTRODUCTION");
                mod.put("estimatedMinutes", 10);
                // Put raw text as content so nothing is lost
                String content = rawText.length() > 5000 ? rawText.substring(0, 5000) : rawText;
                mod.put("content", "The AI response was incomplete. Here is the raw content:\n\n" + content);
                mod.putNull("keyConceptBox");
                fallback.put("totalEstimatedMinutes", 10);
                fallback.putArray("suggestedQuizTopics");
            }
            case "quiz" -> {
                fallback.put("title", "Quiz (generation incomplete)");
                fallback.putArray("questions");
            }
            case "explanation" -> {
                fallback.put("concept", "Explanation incomplete");
                fallback.put("definition", "The AI response was cut short. Please try again.");
                fallback.put("explanation", rawText.length() > 2000 ? rawText.substring(0, 2000) : rawText);
                fallback.putNull("analogy");
                fallback.putNull("codeExamples");
                fallback.putNull("whenToUse");
                fallback.putNull("commonMistakes");
                fallback.putArray("relatedConcepts");
            }
            case "recommendations" -> {
                fallback.putArray("recommendations");
            }
            case "summary" -> {
                fallback.put("title", "Document");
                fallback.put("summary", "Summary generation incomplete.");
                fallback.putArray("mainTopics");
                fallback.putArray("suggestedStudyTopics");
            }
            default -> fallback.put("error", "Parse failed for " + context);
        }

        return fallback;
    }
}
