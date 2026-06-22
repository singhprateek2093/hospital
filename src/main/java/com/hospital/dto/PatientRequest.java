package com.hospital.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/** Body for POST /api/patients (intake registration). */
public record PatientRequest(
        @NotBlank String name,
        @Min(value = 0, message = "age must be 0 or greater") @Max(value = 150, message = "age looks invalid") int age,
        @NotBlank String gender,
        @NotBlank String contact,
        /** Optional: id of the doctor to assign. Null = unassigned. */
        @JsonProperty("assigned_doctor_id") Long assignedDoctorId
) {}
