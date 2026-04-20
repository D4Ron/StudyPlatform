package com.studyplatform.dto.group;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class GroupMemberResponse {
    private UUID id;
    private UUID userId;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private Instant joinedAt;
}
