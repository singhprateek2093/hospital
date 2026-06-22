package com.hospital.dto;

import jakarta.validation.constraints.NotBlank;

/** Body for POST /api/patients/{id}/intake. */
public record IntakeRequest(
        @NotBlank String symptoms,
        String vitals,
        String notes
) {}
