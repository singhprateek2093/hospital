package com.hospital.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Body for POST /api/auth/change-password (the logged-in user changes their own password). */
public record ChangePasswordRequest(
        @NotBlank @JsonProperty("current_password") String currentPassword,
        @NotBlank @Size(min = 6, message = "new password must be at least 6 characters")
        @JsonProperty("new_password") String newPassword
) {}
