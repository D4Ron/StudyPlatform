package com.studyplatform.dto.teacher;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class StudentOverview {
    private UUID userId;
    private String firstName;
    private String lastName;
    private String email;
    private int totalXp;
    private int level;
    private int guidesCompleted;
    private int quizzesTaken;
    private Double averageQuizScore;
    private String status;
}
