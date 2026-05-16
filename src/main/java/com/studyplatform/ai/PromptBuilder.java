package com.studyplatform.ai;

import com.studyplatform.enums.DetailLevel;
import com.studyplatform.enums.Difficulty;
import com.studyplatform.enums.ExpertiseLevel;
import com.studyplatform.enums.QuizType;
import org.springframework.stereotype.Component;

/**
 * Central repository of all prompt templates for AI features.
 * Each method returns a system prompt and builds the user message
 * from the caller's parameters.
 */
@Component
public class PromptBuilder {

    // ══════════════════════════════════════════════════════════
    // STUDY GUIDE GENERATION
    // ══════════════════════════════════════════════════════════

    public static final String GUIDE_SYSTEM_PROMPT = """
            You are an expert educational content creator. You generate structured, 
            comprehensive study guides tailored to the student's expertise level.
            
            Your output must be valid JSON with this exact structure:
            {
              "title": "Guide title",
              "modules": [
                {
                  "title": "Module title",
                  "type": "INTRODUCTION | CONCEPT | EXAMPLE | EXERCISE | SUMMARY",
                  "estimatedMinutes": 10,
                  "content": "Full module content in Markdown format",
                  "keyConceptBox": "One-sentence key takeaway (optional, null if not applicable)"
                }
              ],
              "totalEstimatedMinutes": 60,
              "suggestedQuizTopics": ["topic1", "topic2"]
            }
            
            Rules:
            - Generate 5-10 modules depending on the scope
            - Use Markdown formatting within content (headers, bold, code blocks, lists)
            - For technical topics, include code examples with syntax-highlighted blocks
            - Include analogies and real-world examples
            - Adapt vocabulary and depth to the expertise level
            - Each module should be self-contained but build on previous ones
            - Include practice exercises where appropriate
            - End with a summary module
            - Return ONLY valid JSON, no preamble, no markdown fences
            """;

    public String buildGuideUserMessage(String topic, ExpertiseLevel level,
                                         String specificConcept, String documentText) {
        StringBuilder sb = new StringBuilder();
        sb.append("Generate a study guide on: ").append(topic).append("\n");
        sb.append("Expertise level: ").append(formatLevel(level)).append("\n");

        if (specificConcept != null && !specificConcept.isBlank()) {
            sb.append("Specific focus: ").append(specificConcept).append("\n");
        } else {
            sb.append("Scope: Broad overview of the topic\n");
        }

        if (documentText != null && !documentText.isBlank()) {
            sb.append("\nBase the guide on the following source material:\n");
            sb.append("---BEGIN DOCUMENT---\n");
            sb.append(documentText);
            sb.append("\n---END DOCUMENT---\n");
            sb.append("\nUse this document as the primary source. ");
            sb.append("Structure and explain its content according to the requested level.");
        }

        return sb.toString();
    }

    // ══════════════════════════════════════════════════════════
    // QUIZ GENERATION
    // ══════════════════════════════════════════════════════════

    public static final String QUIZ_SYSTEM_PROMPT = """
            You are an expert assessment creator. You generate quiz questions that test 
            understanding, not just memorization.
            
            Your output must be valid JSON with this exact structure:
            {
              "title": "Quiz title",
              "questions": [
                {
                  "questionText": "The question",
                  "type": "MCQ | TRUE_FALSE | FREE_TEXT",
                  "options": ["A) ...", "B) ...", "C) ...", "D) ..."],
                  "correctAnswer": "A",
                  "explanation": "Why this is correct",
                  "difficulty": "EASY | MEDIUM | HARD",
                  "relatedConcept": "The concept being tested",
                  "points": 10
                }
              ]
            }
            
            Rules:
            - For MCQ: always provide exactly 4 options labeled A, B, C, D
            - For TRUE_FALSE: options are ["True", "False"], correctAnswer is "True" or "False"
            - For FREE_TEXT: options is null, correctAnswer is the expected answer
            - Explanations should teach, not just state the answer
            - Include code snippets in questions when testing programming concepts
            - Vary difficulty within the quiz (mix easy, medium, hard)
            - Return ONLY valid JSON, no preamble, no markdown fences
            """;

    public String buildQuizUserMessage(String topic, Difficulty difficulty,
                                        QuizType quizType, int questionCount,
                                        String guideContent) {
        StringBuilder sb = new StringBuilder();
        sb.append("Generate a ").append(difficulty.name().toLowerCase());
        sb.append(" quiz on: ").append(topic).append("\n");
        sb.append("Number of questions: ").append(questionCount).append("\n");
        sb.append("Quiz type: ").append(formatQuizType(quizType)).append("\n");

        if (guideContent != null && !guideContent.isBlank()) {
            sb.append("\nBase questions on this study material:\n");
            sb.append("---BEGIN CONTENT---\n");
            sb.append(guideContent);
            sb.append("\n---END CONTENT---\n");
            sb.append("\nOnly ask about concepts covered in this content.");
        }

        return sb.toString();
    }

    // ══════════════════════════════════════════════════════════
    // CONCEPT EXPLANATION
    // ══════════════════════════════════════════════════════════

    public static final String EXPLANATION_SYSTEM_PROMPT = """
            You are a patient, expert tutor. You explain concepts clearly and thoroughly, 
            adapting to the requested level of detail.
            
            Your output must be valid JSON with this exact structure:
            {
              "concept": "The concept name",
              "definition": "One-sentence definition",
              "analogy": "A plain-language analogy using everyday objects",
              "explanation": "Full explanation in Markdown format",
              "codeExamples": [
                {
                  "language": "java",
                  "code": "code here",
                  "explanation": "What this code demonstrates"
                }
              ],
              "whenToUse": "Practical guidance on when/why to use this concept",
              "commonMistakes": ["mistake1", "mistake2"],
              "relatedConcepts": ["concept1", "concept2"]
            }
            
            Rules:
            - definition: max 2 sentences
            - analogy: use real-world comparison (cookie cutter, remote control, etc.)
            - codeExamples: include 1-3 depending on detail level, null for non-technical topics
            - commonMistakes: only for MEDIUM and DETAILED levels, null for SHORT
            - Adapt depth and vocabulary to the detail level
            - Return ONLY valid JSON, no preamble, no markdown fences
            """;

    public String buildExplanationUserMessage(String concept, DetailLevel level) {
        return String.format(
                "Explain the following concept: %s\nDetail level: %s\n",
                concept, formatDetailLevel(level));
    }

    // ══════════════════════════════════════════════════════════
    // RECOMMENDATIONS (for solo users)
    // ══════════════════════════════════════════════════════════

    public static final String RECOMMENDATION_SYSTEM_PROMPT = """
            You are a learning advisor. Based on a student's learning history, 
            you suggest specific topics and sub-topics they should study next.
            
            Your output must be valid JSON with this exact structure:
            {
              "recommendations": [
                {
                  "title": "Short title (e.g. 'Inheritance in Java')",
                  "description": "2-3 sentences explaining why this is recommended",
                  "reason": "WEAK_AREA | NATURAL_PROGRESSION | COMPLEMENTARY | UNEXPLORED",
                  "relatedTopic": "The parent topic this relates to",
                  "suggestedAction": "GENERATE_GUIDE | TAKE_QUIZ | EXPLAIN_CONCEPT"
                }
              ]
            }
            
            Rules:
            - Generate exactly 3-5 recommendations
            - Mix different reason types
            - Be specific (not "learn more Java" but "Java Collections Framework")
            - Prioritize weak areas and natural progressions
            - Return ONLY valid JSON, no preamble, no markdown fences
            """;

    public String buildRecommendationUserMessage(String learningProfile) {
        return "Based on this student's learning history, suggest what they should study next:\n\n"
                + learningProfile;
    }

    // ══════════════════════════════════════════════════════════
    // DOCUMENT SUMMARY
    // ══════════════════════════════════════════════════════════

    public static final String SUMMARY_SYSTEM_PROMPT = """
            You are a document summarizer. You create brief, useful summaries of 
            academic documents.
            
            Your output must be valid JSON:
            {
              "title": "Inferred document title",
              "summary": "2-3 paragraph summary",
              "mainTopics": ["topic1", "topic2", "topic3"],
              "suggestedStudyTopics": ["specific topic to generate a guide on"]
            }
            
            Rules:
            - Summary should be 2-3 paragraphs, factual, no fluff
            - mainTopics: 3-5 high-level topics covered
            - suggestedStudyTopics: 2-3 specific topics the student could generate guides for
            - Return ONLY valid JSON, no preamble, no markdown fences
            """;

    public String buildSummaryUserMessage(String documentText) {
        return "Summarize the following document:\n\n"
                + "---BEGIN DOCUMENT---\n"
                + documentText
                + "\n---END DOCUMENT---";
    }

    // ══════════════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════════════

    private String formatLevel(ExpertiseLevel level) {
        return switch (level) {
            case BEGINNER -> "Beginner — no prior knowledge, define every term, use simple analogies";
            case INTERMEDIATE -> "Intermediate — knows basics, go deeper, explain the why not just how";
            case PROFESSIONAL -> "Advanced — strong foundation, cover edge cases, design patterns, best practices";
        };
    }

    private String formatDetailLevel(DetailLevel level) {
        return switch (level) {
            case SHORT -> "Simple — explain like I've never heard of this, no jargon, everyday analogies";
            case MEDIUM -> "Intermediate — proper technical depth with definitions for key terms";
            case DETAILED -> "Advanced — full technical details, edge cases, nuances, multiple code examples";
        };
    }

    private String formatQuizType(QuizType type) {
        return switch (type) {
            case STANDARD -> "Standard MCQ — all multiple choice questions";
            case CUSTOM -> "Custom — mix of MCQ, true/false, and free text";
            case TOPIC_BASED -> "Topic-based practical problem — one main problem to solve step by step";
        };
    }
}
