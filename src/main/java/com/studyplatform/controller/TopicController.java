package com.studyplatform.controller;

import com.studyplatform.dto.topic.CreateTopicRequest;
import com.studyplatform.dto.topic.TopicResponse;
import com.studyplatform.security.CurrentUser;
import com.studyplatform.security.UserPrincipal;
import com.studyplatform.service.TopicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    @PostMapping
    public ResponseEntity<TopicResponse> create(
            @CurrentUser UserPrincipal principal,
            @Valid @RequestBody CreateTopicRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(topicService.create(principal.getUser(), request));
    }

    @GetMapping
    public ResponseEntity<List<TopicResponse>> listMine(@CurrentUser UserPrincipal principal) {
        return ResponseEntity.ok(topicService.listByUser(principal.getId()));
    }

    @GetMapping("/{topicId}")
    public ResponseEntity<TopicResponse> getById(
            @CurrentUser UserPrincipal principal,
            @PathVariable UUID topicId) {
        return ResponseEntity.ok(topicService.getById(topicId, principal.getId()));
    }

    @DeleteMapping("/{topicId}")
    public ResponseEntity<Void> delete(
            @CurrentUser UserPrincipal principal,
            @PathVariable UUID topicId) {
        topicService.delete(topicId, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
