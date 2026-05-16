package com.studyplatform.controller;

import com.studyplatform.dto.guide.GenerateGuideRequest;
import com.studyplatform.dto.guide.GuideListResponse;
import com.studyplatform.dto.guide.GuideResponse;
import com.studyplatform.entity.StudyGuide;
import com.studyplatform.export.PdfExportService;
import com.studyplatform.security.CurrentUser;
import com.studyplatform.security.UserPrincipal;
import com.studyplatform.service.GuideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/guides")
@RequiredArgsConstructor
public class GuideController {

    private final GuideService guideService;
    private final PdfExportService pdfExportService;

    @PostMapping("/generate")
    public ResponseEntity<GuideResponse> generate(
            @CurrentUser UserPrincipal principal,
            @Valid @RequestBody GenerateGuideRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(guideService.generate(principal.getUser(), request));
    }

    @GetMapping
    public ResponseEntity<List<GuideListResponse>> listMine(@CurrentUser UserPrincipal principal) {
        return ResponseEntity.ok(guideService.listByUser(principal.getId()));
    }

    @GetMapping("/{guideId}")
    public ResponseEntity<GuideResponse> getById(
            @CurrentUser UserPrincipal principal,
            @PathVariable UUID guideId) {
        return ResponseEntity.ok(guideService.getById(guideId, principal.getId()));
    }

    @GetMapping("/{guideId}/pdf")
    public ResponseEntity<byte[]> exportPdf(
            @CurrentUser UserPrincipal principal,
            @PathVariable UUID guideId) {

        // Verify access
        guideService.getById(guideId, principal.getId());

        StudyGuide guide = guideService.getEntity(guideId);
        byte[] pdf = pdfExportService.exportGuide(guide);

        String filename = guide.getTitle().replaceAll("[^a-zA-Z0-9 ]", "")
                .replaceAll("\\s+", "_") + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .body(pdf);
    }

    @DeleteMapping("/{guideId}")
    public ResponseEntity<Void> delete(
            @CurrentUser UserPrincipal principal,
            @PathVariable UUID guideId) {
        guideService.delete(guideId, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
