package com.hospital.dto;

import com.hospital.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Body for POST /api/auth/register (admin-only).
 * specialization is required only when role == DOCTOR (validated in the service).
 */
public record RegisterRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, message = "password must be at least 6 characters") String password,
        @NotNull Role role,
        String specialization
) {}
