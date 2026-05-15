package com.studyplatform.dto.quiz;

import com.studyplatform.enums.Difficulty;
import com.studyplatform.enums.QuizType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class GenerateQuizRequest {

    @NotBlank(message = "Topic is required")
    private String topic;

    @NotNull(message = "Difficulty is required")
    private Difficulty difficulty;

    @NotNull(message = "Quiz type is required")
    private QuizType quizType;

    @Min(value = 5, message = "Minimum 5 questions")
    @Max(value = 100, message = "Maximum 100 questions")
    private int questionCount = 15;

    private UUID guideId;
}
