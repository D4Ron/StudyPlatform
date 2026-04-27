package com.studyplatform.storage;

import com.studyplatform.exception.ApiException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "local", matchIfMissing = true)
@Slf4j
public class LocalStorageService implements StorageService {

    @Value("${app.storage.local.path:./uploads}")
    private String storagePath;

    private Path rootLocation;

    @PostConstruct
    public void init() {
        rootLocation = Paths.get(storagePath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootLocation);
            log.info("Local storage initialized at: {}", rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + rootLocation, e);
        }
    }

    @Override
    public String store(MultipartFile file, String key) {
        try {
            Path targetDir = rootLocation.resolve(key).getParent();
            Files.createDirectories(targetDir);

            Path targetFile = rootLocation.resolve(key).normalize();
            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);

            log.info("Stored file locally: {}", key);
            return key;
        } catch (IOException e) {
            throw ApiException.badRequest("Failed to store file: " + e.getMessage());
        }
    }

    @Override
    public InputStream retrieve(String key) {
        try {
            Path file = rootLocation.resolve(key).normalize();
            return Files.newInputStream(file);
        } catch (IOException e) {
            throw ApiException.notFound("File not found: " + key);
        }
    }

    @Override
    public Resource loadAsResource(String key) {
        try {
            Path file = rootLocation.resolve(key).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw ApiException.notFound("File not found: " + key);
        } catch (MalformedURLException e) {
            throw ApiException.notFound("File not found: " + key);
        }
    }

    @Override
    public void delete(String key) {
        try {
            Path file = rootLocation.resolve(key).normalize();
            Files.deleteIfExists(file);
            log.info("Deleted local file: {}", key);
        } catch (IOException e) {
            log.warn("Failed to delete file {}: {}", key, e.getMessage());
        }
    }

    @Override
    public String generateDownloadUrl(String key) {
        // For local storage, return a relative API path
        // The DocumentController has a download endpoint that serves the file
        return "/api/documents/download/" + key;
    }
}
