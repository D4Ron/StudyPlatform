package com.studyplatform.dto.teacher;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class GroupOverview {
    private UUID groupId;
    private String groupName;
    private int memberCount;
    private int totalGroupXp;
    private Double averageQuizScore;
    private List<StudentOverview> students;
    private Instant createdAt;
}
