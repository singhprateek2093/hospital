package com.hospital.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hospital.model.Appointment;

import java.time.Instant;

public record AppointmentResponse(
        Long id,
        @JsonProperty("patient_id") Long patientId,
        @JsonProperty("patient_name") String patientName,
        @JsonProperty("doctor_id") Long doctorId,
        @JsonProperty("doctor_name") String doctorName,
        @JsonProperty("scheduled_at") Instant scheduledAt,
        String reason,
        String status,
        @JsonProperty("created_at") Instant createdAt
) {
    public static AppointmentResponse from(Appointment a) {
        return new AppointmentResponse(
                a.getId(),
                a.getPatient().getId(),
                a.getPatient().getName(),
                a.getDoctor().getId(),
                a.getDoctor().getUser().getName(),
                a.getScheduledAt(),
                a.getReason(),
                a.getStatus().name(),
                a.getCreatedAt());
    }
}
