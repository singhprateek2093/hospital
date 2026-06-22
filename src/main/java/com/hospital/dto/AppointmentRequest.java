package com.hospital.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/** Body for POST /api/appointments. scheduledAt is an ISO-8601 instant (e.g. 2026-07-01T14:30:00Z). */
public record AppointmentRequest(
        @NotNull @JsonProperty("patient_id") Long patientId,
        @NotNull @JsonProperty("doctor_id") Long doctorId,
        @NotNull @JsonProperty("scheduled_at") Instant scheduledAt,
        @NotBlank String reason
) {}
