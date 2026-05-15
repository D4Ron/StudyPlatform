package com.studyplatform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "xp_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class XpLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private Topic topic;

    @Column(nullable = false)
    private int xpEarned;

    @Column(nullable = false)
    private String source;

    private String sourceId;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant earnedAt;
}
