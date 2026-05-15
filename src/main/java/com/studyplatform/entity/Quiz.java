package com.studyplatform.entity;

import com.studyplatform.enums.Difficulty;
import com.studyplatform.enums.QuizType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "quizzes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guide_id")
    private StudyGuide guide;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String topic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuizType quizType;

    private int questionCount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private String questions;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;
}
