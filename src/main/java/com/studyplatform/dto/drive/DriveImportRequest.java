package com.studyplatform.dto.drive;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class DriveImportRequest {
    @NotBlank(message = "Google Drive file ID is required")
    private String driveFileId;

    private UUID groupId;
}
