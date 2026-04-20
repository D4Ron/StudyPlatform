package com.studyplatform.dto.auth;

import com.studyplatform.enums.AccountType;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private AccountType accountType;
}
