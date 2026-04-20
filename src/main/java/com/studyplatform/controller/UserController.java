package com.studyplatform.controller;

import com.studyplatform.entity.User;
import com.studyplatform.security.CurrentUser;
import com.studyplatform.security.UserPrincipal;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<UserProfile> getCurrentUser(@CurrentUser UserPrincipal principal) {
        User user = principal.getUser();

        return ResponseEntity.ok(UserProfile.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .accountType(user.getAccountType().name())
                .educationLevel(user.getEducationLevel() != null
                        ? user.getEducationLevel().name() : null)
                .registrationMode(user.getRegistrationMode() != null
                        ? user.getRegistrationMode().name() : null)
                .preferenceDomains(user.getPreferenceDomains() != null
                        ? Arrays.asList(user.getPreferenceDomains().split(","))
                        : List.of())
                .objectives(user.getObjectives())
                .createdAt(user.getCreatedAt())
                .build());
    }

    @Data
    @Builder
    public static class UserProfile {
        private UUID id;
        private String firstName;
        private String lastName;
        private String email;
        private String accountType;
        private String educationLevel;
        private String registrationMode;
        private List<String> preferenceDomains;
        private String objectives;
        private Instant createdAt;
    }
}
