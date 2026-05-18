package com.studyplatform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "competitions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Competition {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false) private String title;
    @Column(columnDefinition = "TEXT") private String description;
    @Column(nullable = false) private String topic;
    @Column(nullable = false) private Instant startTime;
    @Column(nullable = false) private Instant endTime;
    @Column(nullable = false) private String difficulty;
    private int questionCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by") private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id") private StudyGroup group;

    private boolean active;

    @JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(columnDefinition = "jsonb") private String questions;

    @CreationTimestamp @Column(updatable = false) private Instant createdAt;

    public boolean isOngoing() {
        Instant now = Instant.now();
        return now.isAfter(startTime) && now.isBefore(endTime);
    }
}
