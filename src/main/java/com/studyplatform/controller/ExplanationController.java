package com.studyplatform.controller;

import com.studyplatform.dto.explanation.ExplainConceptRequest;
import com.studyplatform.dto.explanation.ExplanationResponse;
import com.studyplatform.security.CurrentUser;
import com.studyplatform.security.UserPrincipal;
import com.studyplatform.service.ExplanationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/explanations")
@RequiredArgsConstructor
public class ExplanationController {

    private final ExplanationService explanationService;

    @PostMapping
    public ResponseEntity<ExplanationResponse> explain(
            @CurrentUser UserPrincipal principal,
            @Valid @RequestBody ExplainConceptRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(explanationService.explain(principal.getUser(), request));
    }

    @GetMapping
    public ResponseEntity<List<ExplanationResponse>> listMine(@CurrentUser UserPrincipal principal) {
        return ResponseEntity.ok(explanationService.listByUser(principal.getId()));
    }

    @GetMapping("/{explanationId}")
    public ResponseEntity<ExplanationResponse> getById(
            @CurrentUser UserPrincipal principal,
            @PathVariable UUID explanationId) {
        return ResponseEntity.ok(explanationService.getById(explanationId, principal.getId()));
    }

    @DeleteMapping("/{explanationId}")
    public ResponseEntity<Void> delete(
            @CurrentUser UserPrincipal principal,
            @PathVariable UUID explanationId) {
        explanationService.delete(explanationId, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
