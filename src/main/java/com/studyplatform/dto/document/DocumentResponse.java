package com.studyplatform.dto.document;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class DocumentResponse {
    private UUID id;
    private String filename;
    private String contentType;
    private long fileSize;
    private String summary;
    private UUID groupId;
    private String groupName;
    private String uploadedByName;
    private String downloadUrl;
    private Instant uploadedAt;
}
