package com.hospital.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hospital.model.Patient;

import java.time.Instant;

/** Summary view of a patient (list + create responses). */
public record PatientResponse(
        Long id,
        String name,
        int age,
        String gender,
        String contact,
        @JsonProperty("assigned_doctor_id") Long assignedDoctorId,
        @JsonProperty("assigned_doctor_name") String assignedDoctorName,
        @JsonProperty("created_by") Long createdBy,
        @JsonProperty("created_at") Instant createdAt
) {
    public static PatientResponse from(Patient p) {
        Long docId = p.getAssignedDoctor() != null ? p.getAssignedDoctor().getId() : null;
        String docName = p.getAssignedDoctor() != null ? p.getAssignedDoctor().getUser().getName() : null;
        return new PatientResponse(
                p.getId(), p.getName(), p.getAge(), p.getGender(), p.getContact(),
                docId, docName, p.getCreatedBy().getId(), p.getCreatedAt());
    }
}
