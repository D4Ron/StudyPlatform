package com.studyplatform.controller;

import com.studyplatform.dto.quiz.*;
import com.studyplatform.security.CurrentUser;
import com.studyplatform.security.UserPrincipal;
import com.studyplatform.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @PostMapping("/generate")
    public ResponseEntity<QuizResponse> generate(
            @CurrentUser UserPrincipal principal,
            @Valid @RequestBody GenerateQuizRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(quizService.generate(principal.getUser(), request));
    }

    @PostMapping("/submit")
    public ResponseEntity<QuizAttemptResponse> submit(
            @CurrentUser UserPrincipal principal,
            @Valid @RequestBody SubmitQuizRequest request) {
        return ResponseEntity.ok(quizService.submit(principal.getUser(), request));
    }

    @GetMapping
    public ResponseEntity<List<QuizListResponse>> listMine(@CurrentUser UserPrincipal principal) {
        return ResponseEntity.ok(quizService.listByUser(principal.getId()));
    }

    @GetMapping("/{quizId}")
    public ResponseEntity<QuizResponse> getById(
            @CurrentUser UserPrincipal principal,
            @PathVariable UUID quizId) {
        return ResponseEntity.ok(quizService.getById(quizId, principal.getId()));
    }

    @GetMapping("/{quizId}/attempts")
    public ResponseEntity<List<QuizAttemptResponse>> getAttempts(
            @CurrentUser UserPrincipal principal,
            @PathVariable UUID quizId) {
        return ResponseEntity.ok(quizService.getAttempts(quizId, principal.getId()));
    }

    @GetMapping("/attempts/mine")
    public ResponseEntity<List<QuizAttemptResponse>> getMyAttempts(
            @CurrentUser UserPrincipal principal) {
        return ResponseEntity.ok(quizService.getMyAttempts(principal.getId()));
    }

    @DeleteMapping("/{quizId}")
    public ResponseEntity<Void> delete(
            @CurrentUser UserPrincipal principal,
            @PathVariable UUID quizId) {
        quizService.delete(quizId, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
