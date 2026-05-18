package com.studyplatform.dto.competition;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class SubmitCompetitionRequest {
    @NotNull private UUID competitionId;
    @NotNull private List<AnswerSubmission> answers;
    private int timeTakenSeconds;

    @Data
    public static class AnswerSubmission {
        private int questionIndex;
        private String selectedAnswer;
    }
}
