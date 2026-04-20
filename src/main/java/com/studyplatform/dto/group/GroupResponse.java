package com.studyplatform.dto.group;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class GroupResponse {
    private UUID id;
    private String name;
    private String description;
    private String inviteCode;
    private String createdByName;
    private long memberCount;
    private String myRole;
    private Instant createdAt;
}
