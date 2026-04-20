package com.studyplatform.controller;

import com.studyplatform.dto.group.*;
import com.studyplatform.security.CurrentUser;
import com.studyplatform.security.UserPrincipal;
import com.studyplatform.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<GroupResponse> create(
            @CurrentUser UserPrincipal principal,
            @Valid @RequestBody CreateGroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(groupService.create(principal.getUser(), request));
    }

    @PostMapping("/join")
    public ResponseEntity<GroupResponse> join(
            @CurrentUser UserPrincipal principal,
            @Valid @RequestBody JoinGroupRequest request) {
        return ResponseEntity.ok(groupService.join(principal.getUser(), request));
    }

    @GetMapping
    public ResponseEntity<List<GroupResponse>> listMyGroups(@CurrentUser UserPrincipal principal) {
        return ResponseEntity.ok(groupService.listMyGroups(principal.getId()));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponse> getById(
            @CurrentUser UserPrincipal principal,
            @PathVariable UUID groupId) {
        return ResponseEntity.ok(groupService.getById(groupId, principal.getId()));
    }

    @PostMapping("/{groupId}/leave")
    public ResponseEntity<Void> leave(
            @CurrentUser UserPrincipal principal,
            @PathVariable UUID groupId) {
        groupService.leave(principal.getId(), groupId);
        return ResponseEntity.noContent().build();
    }

    // ── Member management ─────────────────────────────────────

    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<GroupMemberResponse>> listMembers(
            @CurrentUser UserPrincipal principal,
            @PathVariable UUID groupId) {
        return ResponseEntity.ok(groupService.listMembers(groupId, principal.getId()));
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<Void> removeMember(
            @CurrentUser UserPrincipal principal,
            @PathVariable UUID groupId,
            @PathVariable UUID userId) {
        groupService.removeMember(groupId, userId, principal.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{groupId}/members/{userId}/promote")
    public ResponseEntity<Map<String, String>> promoteMember(
            @CurrentUser UserPrincipal principal,
            @PathVariable UUID groupId,
            @PathVariable UUID userId) {
        groupService.promoteMember(groupId, userId, principal.getId());
        return ResponseEntity.ok(Map.of("message", "Member promoted to admin"));
    }

    // ── Work sessions ─────────────────────────────────────────

    @PostMapping("/{groupId}/sessions")
    public ResponseEntity<WorkSessionResponse> scheduleSession(
            @CurrentUser UserPrincipal principal,
            @PathVariable UUID groupId,
            @Valid @RequestBody ScheduleSessionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(groupService.scheduleSession(groupId, principal.getUser(), request));
    }

    @GetMapping("/{groupId}/sessions")
    public ResponseEntity<List<WorkSessionResponse>> listSessions(
            @CurrentUser UserPrincipal principal,
            @PathVariable UUID groupId) {
        return ResponseEntity.ok(groupService.listUpcomingSessions(groupId, principal.getId()));
    }
}
