package com.studyplatform.controller;

import com.studyplatform.dto.document.DocumentUploadResponse;
import com.studyplatform.dto.drive.DriveConnectionStatus;
import com.studyplatform.dto.drive.DriveFileResponse;
import com.studyplatform.dto.drive.DriveImportRequest;
import com.studyplatform.security.CurrentUser;
import com.studyplatform.security.UserPrincipal;
import com.studyplatform.service.GoogleDriveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/drive")
@RequiredArgsConstructor
public class GoogleDriveController {

    private final GoogleDriveService driveService;

    @GetMapping("/connect")
    public ResponseEntity<Map<String, String>> getConnectUrl(
            @CurrentUser UserPrincipal principal) {
        String url = driveService.getAuthorizationUrl(principal.getId());
        return ResponseEntity.ok(Map.of("authUrl", url));
    }

    @GetMapping("/callback")
    public ResponseEntity<DriveConnectionStatus> handleCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String userId,
            @CurrentUser UserPrincipal principal) {
        return ResponseEntity.ok(driveService.handleCallback(
                code, principal.getId(), principal.getUser()));
    }

    @GetMapping("/status")
    public ResponseEntity<DriveConnectionStatus> getStatus(
            @CurrentUser UserPrincipal principal) {
        return ResponseEntity.ok(driveService.getStatus(principal.getId()));
    }

    @DeleteMapping("/disconnect")
    public ResponseEntity<Map<String, String>> disconnect(
            @CurrentUser UserPrincipal principal) {
        driveService.disconnect(principal.getId());
        return ResponseEntity.ok(Map.of("message", "Google Drive disconnected"));
    }

    @GetMapping("/files")
    public ResponseEntity<List<DriveFileResponse>> listFiles(
            @CurrentUser UserPrincipal principal,
            @RequestParam(required = false) String query) {
        return ResponseEntity.ok(driveService.listFiles(principal.getId(), query));
    }

    @PostMapping("/import")
    public ResponseEntity<DocumentUploadResponse> importFile(
            @CurrentUser UserPrincipal principal,
            @Valid @RequestBody DriveImportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(driveService.importFile(principal.getUser(), request));
    }
}
