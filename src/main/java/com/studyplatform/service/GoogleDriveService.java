package com.studyplatform.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyplatform.dto.drive.DriveConnectionStatus;
import com.studyplatform.dto.drive.DriveFileResponse;
import com.studyplatform.dto.drive.DriveImportRequest;
import com.studyplatform.dto.document.DocumentUploadResponse;
import com.studyplatform.entity.GoogleDriveConnection;
import com.studyplatform.entity.User;
import com.studyplatform.exception.ApiException;
import com.studyplatform.repository.GoogleDriveConnectionRepository;
import com.studyplatform.storage.StorageService;
import com.studyplatform.entity.Document;
import com.studyplatform.repository.DocumentRepository;
import com.studyplatform.repository.StudyGroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleDriveService {

    private final GoogleDriveConnectionRepository connectionRepository;
    private final DocumentRepository documentRepository;
    private final StudyGroupRepository groupRepository;
    private final GroupService groupService;
    private final StorageService storageService;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.google.client-id:}")
    private String clientId;

    @Value("${app.google.client-secret:}")
    private String clientSecret;

    @Value("${app.google.redirect-uri:http://localhost:8080/api/drive/callback}")
    private String redirectUri;

    private static final String AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String DRIVE_API = "https://www.googleapis.com/drive/v3";
    private static final String USERINFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";

    private static final Set<String> SUPPORTED_MIME_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "text/plain",
            "text/markdown"
    );

    // Google Docs/Sheets/Slides export mime types
    private static final Map<String, String> GOOGLE_EXPORT_TYPES = Map.of(
            "application/vnd.google-apps.document", "application/pdf",
            "application/vnd.google-apps.presentation", "application/pdf",
            "application/vnd.google-apps.spreadsheet", "application/pdf"
    );

    // ── OAuth2 Flow ───────────────────────────────────────────

    public String getAuthorizationUrl(UUID userId) {
        validateConfig();
        return AUTH_URL +
                "?client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&response_type=code" +
                "&scope=https://www.googleapis.com/auth/drive.readonly https://www.googleapis.com/auth/userinfo.email" +
                "&access_type=offline" +
                "&prompt=consent" +
                "&state=" + userId.toString();
    }

    @Transactional
    public DriveConnectionStatus handleCallback(String code, UUID userId, User user) {
        validateConfig();

        // Exchange authorization code for tokens
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        ResponseEntity<JsonNode> response;
        try {
            response = restTemplate.exchange(
                    TOKEN_URL, HttpMethod.POST,
                    new HttpEntity<>(params, headers), JsonNode.class);
        } catch (Exception e) {
            log.error("Google token exchange failed: {}", e.getMessage());
            throw ApiException.badRequest("Failed to connect Google Drive. Please try again.");
        }

        JsonNode body = response.getBody();
        if (body == null || !body.has("access_token")) {
            throw ApiException.badRequest("Invalid response from Google");
        }

        String accessToken = body.get("access_token").asText();
        String refreshToken = body.has("refresh_token") ? body.get("refresh_token").asText() : null;
        int expiresIn = body.has("expires_in") ? body.get("expires_in").asInt() : 3600;

        // Get user email from Google
        String email = fetchGoogleEmail(accessToken);

        // Save or update connection
        GoogleDriveConnection conn = connectionRepository.findByUserId(userId)
                .orElse(GoogleDriveConnection.builder().user(user).build());

        conn.setAccessToken(accessToken);
        if (refreshToken != null) {
            conn.setRefreshToken(refreshToken);
        }
        conn.setTokenExpiry(Instant.now().plusSeconds(expiresIn - 60));
        conn.setEmail(email);

        connectionRepository.save(conn);
        log.info("Google Drive connected for user {} ({})", user.getEmail(), email);

        return DriveConnectionStatus.builder()
                .connected(true)
                .email(email)
                .connectedAt(conn.getConnectedAt())
                .build();
    }

    // ── Connection Status ─────────────────────────────────────

    public DriveConnectionStatus getStatus(UUID userId) {
        return connectionRepository.findByUserId(userId)
                .map(conn -> DriveConnectionStatus.builder()
                        .connected(true)
                        .email(conn.getEmail())
                        .connectedAt(conn.getConnectedAt())
                        .build())
                .orElse(DriveConnectionStatus.builder()
                        .connected(false)
                        .build());
    }

    @Transactional
    public void disconnect(UUID userId) {
        connectionRepository.deleteByUserId(userId);
        log.info("Google Drive disconnected for user {}", userId);
    }

    // ── List Files ────────────────────────────────────────────

    public List<DriveFileResponse> listFiles(UUID userId, String query) {
        String accessToken = getValidAccessToken(userId);

        // Build query — only files the user can see, filter by supported types
        String q = "trashed=false and ("
                + "'application/pdf','application/msword',"
                + "'application/vnd.openxmlformats-officedocument.wordprocessingml.document',"
                + "'application/vnd.openxmlformats-officedocument.presentationml.presentation',"
                + "'application/vnd.ms-powerpoint','text/plain',"
                + "'application/vnd.google-apps.document',"
                + "'application/vnd.google-apps.presentation'"
                + " contains mimeType)";

        // Simplified: just list recent files with supported types
        String url = DRIVE_API + "/files"
                + "?pageSize=30"
                + "&orderBy=modifiedTime desc"
                + "&fields=files(id,name,mimeType,size,modifiedTime,iconLink)"
                + "&q=" + encodeQuery("trashed=false");

        if (query != null && !query.isBlank()) {
            url = DRIVE_API + "/files"
                    + "?pageSize=30"
                    + "&orderBy=modifiedTime desc"
                    + "&fields=files(id,name,mimeType,size,modifiedTime,iconLink)"
                    + "&q=" + encodeQuery("trashed=false and name contains '" + query.replace("'", "\\'") + "'");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<JsonNode> response;
        try {
            response = restTemplate.exchange(url, HttpMethod.GET,
                    new HttpEntity<>(headers), JsonNode.class);
        } catch (Exception e) {
            log.error("Drive list files failed: {}", e.getMessage());
            throw ApiException.badRequest("Failed to list Google Drive files");
        }

        List<DriveFileResponse> files = new ArrayList<>();
        JsonNode body = response.getBody();
        if (body != null && body.has("files")) {
            for (JsonNode file : body.get("files")) {
                String mimeType = file.has("mimeType") ? file.get("mimeType").asText() : "";
                // Only include supported types and Google Docs types
                if (SUPPORTED_MIME_TYPES.contains(mimeType) || GOOGLE_EXPORT_TYPES.containsKey(mimeType)) {
                    files.add(DriveFileResponse.builder()
                            .fileId(file.get("id").asText())
                            .name(file.has("name") ? file.get("name").asText() : "Unnamed")
                            .mimeType(mimeType)
                            .size(file.has("size") ? file.get("size").asLong() : 0)
                            .modifiedTime(file.has("modifiedTime") ? file.get("modifiedTime").asText() : "")
                            .iconLink(file.has("iconLink") ? file.get("iconLink").asText() : "")
                            .build());
                }
            }
        }

        return files;
    }

    // ── Import File ───────────────────────────────────────────

    @Transactional
    public DocumentUploadResponse importFile(User user, DriveImportRequest request) {
        String accessToken = getValidAccessToken(user.getId());

        // Get file metadata first
        String metaUrl = DRIVE_API + "/files/" + request.getDriveFileId()
                + "?fields=id,name,mimeType,size";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<JsonNode> metaResponse;
        try {
            metaResponse = restTemplate.exchange(metaUrl, HttpMethod.GET,
                    new HttpEntity<>(headers), JsonNode.class);
        } catch (Exception e) {
            throw ApiException.notFound("File not found in Google Drive");
        }

        JsonNode meta = metaResponse.getBody();
        String fileName = meta.get("name").asText();
        String mimeType = meta.get("mimeType").asText();
        long fileSize = meta.has("size") ? meta.get("size").asLong() : 0;

        // Download the file content
        byte[] fileBytes;
        String finalMimeType;

        if (GOOGLE_EXPORT_TYPES.containsKey(mimeType)) {
            // Google Docs/Sheets/Slides — export as PDF
            finalMimeType = "application/pdf";
            fileName = fileName + ".pdf";
            String exportUrl = DRIVE_API + "/files/" + request.getDriveFileId()
                    + "/export?mimeType=application/pdf";
            fileBytes = downloadBytes(exportUrl, accessToken);
        } else {
            // Regular file — download directly
            finalMimeType = mimeType;
            String downloadUrl = DRIVE_API + "/files/" + request.getDriveFileId() + "?alt=media";
            fileBytes = downloadBytes(downloadUrl, accessToken);
        }

        fileSize = fileBytes.length;

        // Store in our storage (local or S3)
        String prefix = request.getGroupId() != null
                ? "groups/" + request.getGroupId() + "/documents/"
                : "users/" + user.getId() + "/documents/";
        String key = prefix + UUID.randomUUID() + "_" + sanitize(fileName);

        storageService.storeBytes(fileBytes, key);

        // Create Document entity
        com.studyplatform.entity.StudyGroup group = null;
        if (request.getGroupId() != null) {
            group = groupRepository.findById(request.getGroupId())
                    .orElseThrow(() -> ApiException.notFound("Group not found"));
            groupService.requireMembership(user.getId(), request.getGroupId());
        }

        Document doc = Document.builder()
                .uploadedBy(user)
                .group(group)
                .filename(fileName)
                .contentType(finalMimeType)
                .storageKey(key)
                .fileSize(fileSize)
                .build();

        doc = documentRepository.save(doc);
        log.info("Imported '{}' from Google Drive for user {}", fileName, user.getEmail());

        return DocumentUploadResponse.builder()
                .documentId(doc.getId())
                .filename(fileName)
                .contentType(finalMimeType)
                .fileSize(fileSize)
                .message("File imported from Google Drive")
                .build();
    }

    // ── Token Management ──────────────────────────────────────

    private String getValidAccessToken(UUID userId) {
        GoogleDriveConnection conn = connectionRepository.findByUserId(userId)
                .orElseThrow(() -> ApiException.badRequest(
                        "Google Drive is not connected. Please connect first."));

        if (!conn.isTokenExpired()) {
            return conn.getAccessToken();
        }

        // Refresh the token
        return refreshAccessToken(conn);
    }

    private String refreshAccessToken(GoogleDriveConnection conn) {
        validateConfig();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("refresh_token", conn.getRefreshToken());
        params.add("grant_type", "refresh_token");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    TOKEN_URL, HttpMethod.POST,
                    new HttpEntity<>(params, headers), JsonNode.class);

            JsonNode body = response.getBody();
            if (body == null || !body.has("access_token")) {
                throw new RuntimeException("No access token in refresh response");
            }

            String newToken = body.get("access_token").asText();
            int expiresIn = body.has("expires_in") ? body.get("expires_in").asInt() : 3600;

            conn.setAccessToken(newToken);
            conn.setTokenExpiry(Instant.now().plusSeconds(expiresIn - 60));
            connectionRepository.save(conn);

            log.info("Refreshed Google Drive token for user {}", conn.getUser().getEmail());
            return newToken;

        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            throw ApiException.unauthorized(
                    "Google Drive session expired. Please reconnect.");
        }
    }

    // ── Helpers ───────────────────────────────────────────────

    private String fetchGoogleEmail(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    USERINFO_URL, HttpMethod.GET,
                    new HttpEntity<>(headers), JsonNode.class);
            JsonNode body = response.getBody();
            return body != null && body.has("email") ? body.get("email").asText() : "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }

    private byte[] downloadBytes(String url, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    url, HttpMethod.GET,
                    new HttpEntity<>(headers), byte[].class);
            if (response.getBody() == null) {
                throw new RuntimeException("Empty response");
            }
            return response.getBody();
        } catch (Exception e) {
            log.error("Drive file download failed: {}", e.getMessage());
            throw ApiException.badRequest("Failed to download file from Google Drive");
        }
    }

    private void validateConfig() {
        if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank()) {
            throw ApiException.badRequest(
                    "Google Drive integration is not configured. Set app.google.client-id and app.google.client-secret.");
        }
    }

    private String sanitize(String filename) {
        if (filename == null) return "unnamed";
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String encodeQuery(String q) {
        try {
            return java.net.URLEncoder.encode(q, "UTF-8");
        } catch (Exception e) {
            return q;
        }
    }
}
