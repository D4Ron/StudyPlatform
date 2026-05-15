package com.studyplatform.entity;

import com.studyplatform.enums.ExpertiseLevel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "study_guides")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudyGuide {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private Topic topic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private Document document;

    @Column(nullable = false)
    private String title;

    private String specificConcept;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpertiseLevel expertiseLevel;

    private boolean broadOverview;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private String content;

    private int totalEstimatedMinutes;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;
}
