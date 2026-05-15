package com.studyplatform.controller;

import com.studyplatform.dto.recommendation.RecommendationResponse;
import com.studyplatform.security.CurrentUser;
import com.studyplatform.security.UserPrincipal;
import com.studyplatform.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping
    public ResponseEntity<List<RecommendationResponse>> getRecommendations(
            @CurrentUser UserPrincipal principal) {
        return ResponseEntity.ok(recommendationService.getRecommendations(principal.getId()));
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> triggerGeneration(
            @CurrentUser UserPrincipal principal) {
        recommendationService.generateRecommendations(principal.getUser());
        return ResponseEntity.ok(Map.of(
                "message", "Recommendation generation started. Check back in a few seconds."));
    }

    @PostMapping("/{recommendationId}/acted")
    public ResponseEntity<Void> markActedOn(
            @CurrentUser UserPrincipal principal,
            @PathVariable UUID recommendationId) {
        recommendationService.markActedOn(recommendationId, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
