package com.studyplatform.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class SendMessageRequest {
    @NotNull(message = "Group ID is required")
    private UUID groupId;
    @NotBlank(message = "Message content is required")
    private String content;
}
