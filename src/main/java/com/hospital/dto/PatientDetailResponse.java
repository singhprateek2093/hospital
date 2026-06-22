package com.hospital.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hospital.model.Patient;

import java.time.Instant;
import java.util.List;

/**
 * Full view of a patient for GET /api/patients/{id}, including the intake history.
 * (The contract showed a single "intake"; we return the full list as "intake_forms"
 * since a patient can have multiple visits.)
 */
public record PatientDetailResponse(
        Long id,
        String name,
        int age,
        String gender,
        String contact,
        @JsonProperty("assigned_doctor_id") Long assignedDoctorId,
        @JsonProperty("assigned_doctor_name") String assignedDoctorName,
        @JsonProperty("created_by") Long createdBy,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("intake_forms") List<IntakeResponse> intakeForms
) {
    public static PatientDetailResponse from(Patient p) {
        Long docId = p.getAssignedDoctor() != null ? p.getAssignedDoctor().getId() : null;
        String docName = p.getAssignedDoctor() != null ? p.getAssignedDoctor().getUser().getName() : null;
        List<IntakeResponse> forms = p.getIntakeForms().stream()
                .map(IntakeResponse::from)
                .toList();
        return new PatientDetailResponse(
                p.getId(), p.getName(), p.getAge(), p.getGender(), p.getContact(),
                docId, docName, p.getCreatedBy().getId(), p.getCreatedAt(), forms);
    }
}
