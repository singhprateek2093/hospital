package com.hospital.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hospital.model.IntakeForm;

import java.time.Instant;

/** An intake/visit record in responses. */
public record IntakeResponse(
        Long id,
        @JsonProperty("patient_id") Long patientId,
        String symptoms,
        String vitals,
        String notes,
        Instant date
) {
    public static IntakeResponse from(IntakeForm f) {
        return new IntakeResponse(
                f.getId(), f.getPatient().getId(),
                f.getSymptoms(), f.getVitals(), f.getNotes(), f.getDate());
    }
}
