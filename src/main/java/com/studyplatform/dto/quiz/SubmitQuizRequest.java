package com.studyplatform.dto.quiz;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class SubmitQuizRequest {

    @NotNull(message = "Quiz ID is required")
    private UUID quizId;

    @NotNull(message = "Answers are required")
    private List<AnswerSubmission> answers;

    private int timeTakenSeconds;

    @Data
    public static class AnswerSubmission {
        private int questionIndex;
        private String selectedAnswer;
    }
}
