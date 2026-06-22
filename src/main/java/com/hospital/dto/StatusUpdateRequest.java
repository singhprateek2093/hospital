package com.hospital.dto;

import com.hospital.model.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

/** Body for PATCH /api/appointments/{id}/status. */
public record StatusUpdateRequest(
        @NotNull AppointmentStatus status
) {}
