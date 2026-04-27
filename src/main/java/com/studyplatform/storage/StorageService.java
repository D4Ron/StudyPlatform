package com.studyplatform.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface StorageService {

    /**
     * Store a file and return the storage key.
     */
    String store(MultipartFile file, String key);

    /**
     * Get an InputStream for reading the file (used by Tika for text extraction).
     */
    InputStream retrieve(String key);

    /**
     * Get a downloadable Resource for streaming to the client.
     */
    Resource loadAsResource(String key);

    /**
     * Delete a file by its storage key.
     */
    void delete(String key);

    /**
     * Generate a URL for downloading the file.
     * For local storage: returns a relative API path.
     * For S3: returns a presigned URL.
     */
    String generateDownloadUrl(String key);
}
