package com.studyplatform.service;

import com.studyplatform.dto.document.DocumentResponse;
import com.studyplatform.dto.document.DocumentUploadResponse;
import com.studyplatform.entity.Document;
import com.studyplatform.entity.StudyGroup;
import com.studyplatform.entity.User;
import com.studyplatform.exception.ApiException;
import com.studyplatform.repository.DocumentRepository;
import com.studyplatform.repository.StudyGroupRepository;
import com.studyplatform.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final StudyGroupRepository groupRepository;
    private final GroupService groupService;
    private final StorageService storageService;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "text/plain",
            "text/markdown"
    );

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB

    // ── Upload ────────────────────────────────────────────────

    @Transactional
    public DocumentUploadResponse upload(MultipartFile file, UUID groupId, User user) {
        validateFile(file);

        StudyGroup group = null;
        if (groupId != null) {
            group = groupRepository.findById(groupId)
                    .orElseThrow(() -> ApiException.notFound("Group not found"));
            groupService.requireMembership(user.getId(), groupId);
        }

        // Build storage key: users/{userId}/documents/{uuid}_{filename}
        // or groups/{groupId}/documents/{uuid}_{filename}
        String prefix = groupId != null
                ? "groups/" + groupId + "/documents/"
                : "users/" + user.getId() + "/documents/";
        String key = prefix + UUID.randomUUID() + "_" + sanitizeFilename(file.getOriginalFilename());

        storageService.store(file, key);

        Document doc = Document.builder()
                .uploadedBy(user)
                .group(group)
                .filename(file.getOriginalFilename())
                .contentType(file.getContentType())
                .storageKey(key)
                .fileSize(file.getSize())
                .build();

        doc = documentRepository.save(doc);
        log.info("Document uploaded: {} by user {}", doc.getFilename(), user.getEmail());

        return DocumentUploadResponse.builder()
                .documentId(doc.getId())
                .filename(doc.getFilename())
                .contentType(doc.getContentType())
                .fileSize(doc.getFileSize())
                .message("Document uploaded successfully")
                .build();
    }

    // ── List ──────────────────────────────────────────────────

    public List<DocumentResponse> listMyDocuments(UUID userId) {
        return documentRepository.findByUploadedByIdAndGroupIsNullOrderByUploadedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<DocumentResponse> listGroupDocuments(UUID groupId, UUID userId) {
        groupService.requireMembership(userId, groupId);
        return documentRepository.findByGroupIdOrderByUploadedAtDesc(groupId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ── Get one ───────────────────────────────────────────────

    public DocumentResponse getById(UUID documentId, UUID userId) {
        Document doc = findAndAuthorize(documentId, userId);
        return toResponse(doc);
    }

    // ── Download ──────────────────────────────────────────────

    public Resource download(UUID documentId, UUID userId) {
        Document doc = findAndAuthorize(documentId, userId);
        return storageService.loadAsResource(doc.getStorageKey());
    }

    public Document getDocumentEntity(UUID documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> ApiException.notFound("Document not found"));
    }

    // ── Delete ────────────────────────────────────────────────

    @Transactional
    public void delete(UUID documentId, UUID userId) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> ApiException.notFound("Document not found"));

        // Only the uploader can delete
        if (!doc.getUploadedBy().getId().equals(userId)) {
            throw ApiException.forbidden("Only the uploader can delete this document");
        }

        storageService.delete(doc.getStorageKey());
        documentRepository.delete(doc);
        log.info("Document deleted: {} by user {}", doc.getFilename(), userId);
    }

    // ── Helpers ───────────────────────────────────────────────

    private Document findAndAuthorize(UUID documentId, UUID userId) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> ApiException.notFound("Document not found"));

        // Personal document — only owner can access
        if (doc.getGroup() == null) {
            if (!doc.getUploadedBy().getId().equals(userId)) {
                throw ApiException.forbidden("You do not have access to this document");
            }
            return doc;
        }

        // Group document — any group member can access
        groupService.requireMembership(userId, doc.getGroup().getId());
        return doc;
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw ApiException.badRequest("File is empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw ApiException.badRequest("File size exceeds 50MB limit");
        }
        if (file.getContentType() == null || !ALLOWED_TYPES.contains(file.getContentType())) {
            throw ApiException.badRequest(
                    "Unsupported file format. Allowed: PDF, DOCX, DOC, PPTX, PPT, TXT, MD");
        }
    }

    private String sanitizeFilename(String filename) {
        if (filename == null) return "unnamed";
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private DocumentResponse toResponse(Document doc) {
        return DocumentResponse.builder()
                .id(doc.getId())
                .filename(doc.getFilename())
                .contentType(doc.getContentType())
                .fileSize(doc.getFileSize())
                .summary(doc.getSummary())
                .groupId(doc.getGroup() != null ? doc.getGroup().getId() : null)
                .groupName(doc.getGroup() != null ? doc.getGroup().getName() : null)
                .uploadedByName(doc.getUploadedBy().getFullName())
                .downloadUrl(storageService.generateDownloadUrl(doc.getStorageKey()))
                .uploadedAt(doc.getUploadedAt())
                .build();
    }
}
