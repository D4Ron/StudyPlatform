package com.studyplatform.ai;

import com.studyplatform.exception.ApiException;
import com.studyplatform.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentExtractor {

    private final StorageService storageService;
    private final Tika tika = new Tika();

    // Max characters to extract — Claude has context limits
    // and very long docs should be chunked in production
    private static final int MAX_CHARS = 80_000;

    /**
     * Extract text content from a stored document.
     * Works with PDF, DOCX, DOC, PPTX, PPT, TXT, MD.
     */
    public String extractText(String storageKey) {
        try (InputStream stream = storageService.retrieve(storageKey)) {
            String text = tika.parseToString(stream);

            if (text == null || text.isBlank()) {
                throw ApiException.badRequest(
                        "Could not extract text from document. It may be image-only or corrupted.");
            }

            // Trim to max length if needed
            if (text.length() > MAX_CHARS) {
                text = text.substring(0, MAX_CHARS);
                log.warn("Document {} truncated to {} chars", storageKey, MAX_CHARS);
            }

            log.info("Extracted {} chars from {}", text.length(), storageKey);
            return text.trim();

        } catch (TikaException e) {
            log.error("Tika extraction failed for {}: {}", storageKey, e.getMessage());
            throw ApiException.badRequest("Failed to parse document: " + e.getMessage());
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error extracting {}: {}", storageKey, e.getMessage());
            throw ApiException.badRequest("Failed to read document");
        }
    }

    /**
     * Extract text and return a truncated preview (for summaries, suggestions).
     */
    public String extractPreview(String storageKey, int maxChars) {
        String full = extractText(storageKey);
        if (full.length() <= maxChars) return full;
        return full.substring(0, maxChars) + "...";
    }
}
