package com.studyplatform.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Instant;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class HealthController {

    private final DataSource dataSource;

    @GetMapping("/actuator/health")
    public ResponseEntity<Map<String, Object>> health() {
        boolean dbUp = false;
        try (Connection conn = dataSource.getConnection()) {
            dbUp = conn.isValid(2);
        } catch (Exception ignored) {}

        String status = dbUp ? "UP" : "DEGRADED";

        return ResponseEntity.ok(Map.of(
                "status", status,
                "timestamp", Instant.now().toString(),
                "database", dbUp ? "connected" : "disconnected",
                "service", "study-platform"
        ));
    }
}
