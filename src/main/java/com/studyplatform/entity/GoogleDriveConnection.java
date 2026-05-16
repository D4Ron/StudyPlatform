package com.studyplatform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "google_drive_connections")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GoogleDriveConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String accessToken;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String refreshToken;

    @Column(nullable = false)
    private Instant tokenExpiry;

    private String email;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant connectedAt;

    @UpdateTimestamp
    private Instant updatedAt;

    public boolean isTokenExpired() {
        return Instant.now().isAfter(tokenExpiry);
    }
}
