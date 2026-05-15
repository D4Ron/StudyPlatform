package com.studyplatform.entity;

import com.studyplatform.enums.DetailLevel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "concept_explanations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ConceptExplanation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String concept;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DetailLevel detailLevel;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private String content;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;
}
