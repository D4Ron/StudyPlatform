package com.studyplatform.websocket;

import com.studyplatform.dto.chat.ChatMessageResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.util.UUID;

/**
 * WebSocket message handler for real-time features.
 *
 * Channels:
 * - /topic/chat/{groupId}     — group chat messages
 * - /topic/notes/{groupId}    — note edit broadcasts
 * - /topic/pomodoro/{groupId} — shared Pomodoro timer state
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketMessageHandler {

    private final SimpMessagingTemplate messagingTemplate;

    // ── Chat ──────────────────────────────────────────────────

    @MessageMapping("/chat/{groupId}")
    @SendTo("/topic/chat/{groupId}")
    public ChatMessageResponse handleChatMessage(
            @DestinationVariable String groupId,
            WsChatMessage message) {

        log.info("WS chat in group {}: {} says '{}'",
                groupId, message.getSenderName(), message.getContent());

        return ChatMessageResponse.builder()
                .id(UUID.randomUUID())
                .groupId(UUID.fromString(groupId))
                .senderId(UUID.fromString(message.getSenderId()))
                .senderName(message.getSenderName())
                .content(message.getContent())
                .sentAt(Instant.now())
                .build();
    }

    // ── Notes ─────────────────────────────────────────────────

    @MessageMapping("/notes/{groupId}")
    @SendTo("/topic/notes/{groupId}")
    public WsNoteUpdate handleNoteUpdate(
            @DestinationVariable String groupId,
            WsNoteUpdate update) {

        log.debug("WS note update in group {}: note {} by {}",
                groupId, update.getNoteId(), update.getEditorName());
        return update;
    }

    // ── Pomodoro ──────────────────────────────────────────────

    @MessageMapping("/pomodoro/{groupId}")
    @SendTo("/topic/pomodoro/{groupId}")
    public WsPomodoroState handlePomodoroAction(
            @DestinationVariable String groupId,
            WsPomodoroState state) {

        log.info("WS pomodoro in group {}: action={}, minutes={}",
                groupId, state.getAction(), state.getMinutes());
        state.setTimestamp(Instant.now().toString());
        return state;
    }

    // ── WebSocket message types ───────────────────────────────

    @Data
    public static class WsChatMessage {
        private String senderId;
        private String senderName;
        private String content;
    }

    @Data
    public static class WsNoteUpdate {
        private String noteId;
        private String editorId;
        private String editorName;
        private String content;
        private String action; // EDIT, CURSOR_MOVE
    }

    @Data
    public static class WsPomodoroState {
        private String action; // START, PAUSE, RESET, COMPLETE
        private int minutes;
        private String startedBy;
        private String timestamp;
    }
}
