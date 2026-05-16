package com.studyplatform.dto.drive;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class DriveConnectionStatus {
    private boolean connected;
    private String email;
    private Instant connectedAt;
}
