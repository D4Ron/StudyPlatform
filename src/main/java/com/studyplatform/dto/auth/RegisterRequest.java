package com.studyplatform.dto.auth;

import com.studyplatform.enums.AccountType;
import com.studyplatform.enums.EducationLevel;
import com.studyplatform.enums.RegistrationMode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class RegisterRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    private EducationLevel educationLevel;

    private RegistrationMode registrationMode;

    // Flexible list of preference domains (e.g. ["COMPUTER_SCIENCE", "MATHEMATICS"])
    private List<String> preferenceDomains;

    private String monitoredFolder;

    private String objectives;

    // If registrationMode is JOIN_GROUP, this holds the invite code
    private String groupInviteCode;
}
