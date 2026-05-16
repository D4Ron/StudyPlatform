package com.studyplatform.dto.chat;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ChatMessageResponse {
    private UUID id;
    private UUID groupId;
    private UUID senderId;
    private String senderName;
    private String content;
    private Instant sentAt;
}
