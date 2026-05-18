package com.studyplatform.controller;

import com.studyplatform.dto.competition.*;
import com.studyplatform.security.CurrentUser;
import com.studyplatform.security.UserPrincipal;
import com.studyplatform.service.CompetitionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/competitions")
@RequiredArgsConstructor
public class CompetitionController {

    private final CompetitionService competitionService;

    @PostMapping
    public ResponseEntity<CompetitionResponse> create(
            @CurrentUser UserPrincipal principal,
            @Valid @RequestBody CreateCompetitionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(competitionService.create(principal.getUser(), request));
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<Map<String, String>> join(
            @CurrentUser UserPrincipal principal,
            @PathVariable UUID id) {
        competitionService.join(id, principal.getUser());
        return ResponseEntity.ok(Map.of("message", "Joined competition successfully"));
    }

    @PostMapping("/submit")
    public ResponseEntity<LeaderboardEntry> submit(
            @CurrentUser UserPrincipal principal,
            @Valid @RequestBody SubmitCompetitionRequest request) {
        return ResponseEntity.ok(competitionService.submit(principal.getUser(), request));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<CompetitionResponse>> listUpcoming(@CurrentUser UserPrincipal principal) {
        return ResponseEntity.ok(competitionService.listUpcoming(principal.getId()));
    }

    @GetMapping("/ongoing")
    public ResponseEntity<List<CompetitionResponse>> listOngoing(@CurrentUser UserPrincipal principal) {
        return ResponseEntity.ok(competitionService.listOngoing(principal.getId()));
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<CompetitionResponse>> listByGroup(
            @CurrentUser UserPrincipal principal,
            @PathVariable UUID groupId) {
        return ResponseEntity.ok(competitionService.listByGroup(groupId, principal.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompetitionResponse> getById(
            @CurrentUser UserPrincipal principal,
            @PathVariable UUID id) {
        return ResponseEntity.ok(competitionService.getById(id, principal.getId()));
    }

    @GetMapping("/{id}/leaderboard")
    public ResponseEntity<List<LeaderboardEntry>> getLeaderboard(@PathVariable UUID id) {
        return ResponseEntity.ok(competitionService.getLeaderboard(id));
    }
}
