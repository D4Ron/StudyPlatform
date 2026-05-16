package com.studyplatform.controller;

import com.studyplatform.dto.stats.DashboardStatsResponse;
import com.studyplatform.dto.stats.StudySessionRequest;
import com.studyplatform.security.CurrentUser;
import com.studyplatform.security.UserPrincipal;
import com.studyplatform.service.StatsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStatsResponse> getDashboard(
            @CurrentUser UserPrincipal principal) {
        return ResponseEntity.ok(statsService.getDashboardStats(principal.getId()));
    }

    @PostMapping("/sessions")
    public ResponseEntity<Map<String, String>> logSession(
            @CurrentUser UserPrincipal principal,
            @Valid @RequestBody StudySessionRequest request) {
        statsService.logSession(principal.getUser(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Study session logged"));
    }
}
