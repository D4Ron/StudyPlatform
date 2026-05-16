package com.studyplatform.dto.note;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class NoteResponse {
    private UUID id;
    private String title;
    private String content;
    private String authorName;
    private UUID groupId;
    private String groupName;
    private boolean sharedWithGroup;
    private Instant createdAt;
    private Instant updatedAt;
}
