package com.studyplatform.dto.drive;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DriveFileResponse {
    private String fileId;
    private String name;
    private String mimeType;
    private long size;
    private String modifiedTime;
    private String iconLink;
}
