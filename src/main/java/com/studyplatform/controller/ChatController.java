package com.studyplatform.controller;

import com.studyplatform.dto.chat.ChatMessageResponse;
import com.studyplatform.dto.chat.SendMessageRequest;
import com.studyplatform.security.CurrentUser;
import com.studyplatform.security.UserPrincipal;
import com.studyplatform.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ChatMessageResponse> send(
            @CurrentUser UserPrincipal principal,
            @Valid @RequestBody SendMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chatService.send(principal.getUser(), request));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<List<ChatMessageResponse>> getHistory(
            @CurrentUser UserPrincipal principal,
            @PathVariable UUID groupId,
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(chatService.getHistory(groupId, principal.getId(), limit));
    }
}
