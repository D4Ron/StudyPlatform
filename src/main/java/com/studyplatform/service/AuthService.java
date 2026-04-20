package com.studyplatform.service;

import com.studyplatform.dto.auth.*;
import com.studyplatform.dto.group.JoinGroupRequest;
import com.studyplatform.entity.User;
import com.studyplatform.enums.RegistrationMode;
import com.studyplatform.exception.ApiException;
import com.studyplatform.repository.UserRepository;
import com.studyplatform.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final GroupService groupService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw ApiException.conflict("An account with this email already exists");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .accountType(request.getAccountType())
                .educationLevel(request.getEducationLevel())
                .registrationMode(request.getRegistrationMode())
                .preferenceDomains(
                        request.getPreferenceDomains() != null
                                ? String.join(",", request.getPreferenceDomains())
                                : null)
                .monitoredFolder(request.getMonitoredFolder())
                .objectives(request.getObjectives())
                .build();

        user = userRepository.save(user);
        log.info("New user registered: {} ({})", user.getEmail(), user.getAccountType());

        // Auto-join group if invite code provided during registration
        if (request.getRegistrationMode() == RegistrationMode.JOIN_GROUP
                && request.getGroupInviteCode() != null
                && !request.getGroupInviteCode().isBlank()) {
            try {
                JoinGroupRequest joinRequest = new JoinGroupRequest();
                joinRequest.setInviteCode(request.getGroupInviteCode());
                groupService.join(user, joinRequest);
                log.info("User {} auto-joined group via invite code", user.getEmail());
            } catch (Exception e) {
                log.warn("Auto-join failed for user {}: {}", user.getEmail(), e.getMessage());
                // Don't fail registration if group join fails
            }
        }

        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail().toLowerCase().trim(),
                            request.getPassword()));
        } catch (BadCredentialsException e) {
            throw ApiException.unauthorized("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> ApiException.unauthorized("Invalid email or password"));

        log.info("User logged in: {}", user.getEmail());
        return buildAuthResponse(user);
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!tokenProvider.validateToken(refreshToken)) {
            throw ApiException.unauthorized("Invalid or expired refresh token");
        }

        if (!"refresh".equals(tokenProvider.getTokenType(refreshToken))) {
            throw ApiException.unauthorized("Token is not a refresh token");
        }

        String email = tokenProvider.getEmailFromToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> ApiException.unauthorized("User not found"));

        return buildAuthResponse(user);
    }

    public void requestPasswordReset(PasswordResetRequest request) {
        // For now, just verify the email exists.
        // In production: generate a reset token, store it with expiry,
        // and send an email with a reset link.
        userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> ApiException.notFound("No account found with this email"));

        log.info("Password reset requested for: {}", request.getEmail());
        // TODO: Implement email sending with reset token
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = tokenProvider.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = tokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .accountType(user.getAccountType())
                .build();
    }
}
