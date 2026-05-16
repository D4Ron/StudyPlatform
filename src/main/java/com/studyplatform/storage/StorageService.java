package com.studyplatform.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface StorageService {

    String store(MultipartFile file, String key);

    /**
     * Store raw bytes — used for Google Drive imports and other programmatic uploads.
     */
    String storeBytes(byte[] data, String key);

    InputStream retrieve(String key);

    Resource loadAsResource(String key);

    void delete(String key);

    String generateDownloadUrl(String key);
}
