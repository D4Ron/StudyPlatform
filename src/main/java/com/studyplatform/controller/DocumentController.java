package com.studyplatform.controller;

import com.studyplatform.dto.document.DocumentResponse;
import com.studyplatform.dto.document.DocumentUploadResponse;
import com.studyplatform.security.CurrentUser;
import com.studyplatform.security.UserPrincipal;
import com.studyplatform.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    // ── Upload (multipart) ────────────────────────────────────

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentUploadResponse> upload(
            @CurrentUser UserPrincipal principal,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "groupId", required = false) UUID groupId) {

        DocumentUploadResponse response = documentService.upload(file, groupId, principal.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── List my personal documents (no group) ─────────────────

    @GetMapping("/mine")
    public ResponseEntity<List<DocumentResponse>> listMine(@CurrentUser UserPrincipal principal) {
        return ResponseEntity.ok(documentService.listMyDocuments(principal.getId()));
    }

    // ── List group documents ──────────────────────────────────

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<DocumentResponse>> listGroupDocuments(
            @CurrentUser UserPrincipal principal,
            @PathVariable UUID groupId) {
        return ResponseEntity.ok(documentService.listGroupDocuments(groupId, principal.getId()));
    }

    // ── Get document details ──────────────────────────────────

    @GetMapping("/{documentId}")
    public ResponseEntity<DocumentResponse> getById(
            @CurrentUser UserPrincipal principal,
            @PathVariable UUID documentId) {
        return ResponseEntity.ok(documentService.getById(documentId, principal.getId()));
    }

    // ── Download file ─────────────────────────────────────────

    @GetMapping("/{documentId}/download")
    public ResponseEntity<Resource> download(
            @CurrentUser UserPrincipal principal,
            @PathVariable UUID documentId) {

        DocumentResponse doc = documentService.getById(documentId, principal.getId());
        Resource resource = documentService.download(documentId, principal.getId());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(doc.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + doc.getFilename() + "\"")
                .body(resource);
    }

    // ── Delete ────────────────────────────────────────────────

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> delete(
            @CurrentUser UserPrincipal principal,
            @PathVariable UUID documentId) {
        documentService.delete(documentId, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
