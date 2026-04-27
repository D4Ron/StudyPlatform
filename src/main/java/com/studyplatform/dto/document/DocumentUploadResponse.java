package com.studyplatform.dto.document;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class DocumentUploadResponse {
    private UUID documentId;
    private String filename;
    private String contentType;
    private long fileSize;
    private String message;
}
