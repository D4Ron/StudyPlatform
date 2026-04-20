package com.studyplatform.entity;

import com.studyplatform.enums.AccountType;
import com.studyplatform.enums.EducationLevel;
import com.studyplatform.enums.RegistrationMode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    private EducationLevel educationLevel;

    @Enumerated(EnumType.STRING)
    private RegistrationMode registrationMode;

    // Comma-separated preference domains (extensible, not a fixed enum)
    // e.g. "COMPUTER_SCIENCE,MATHEMATICS"
    private String preferenceDomains;

    // Path the user chose to monitor on their device
    private String monitoredFolder;

    // Free-text learning objectives
    @Column(columnDefinition = "TEXT")
    private String objectives;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
