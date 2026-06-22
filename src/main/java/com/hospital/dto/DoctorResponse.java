package com.hospital.dto;

import com.hospital.model.Doctor;

/** Item in GET /api/doctors — used to populate the assignment dropdown. */
public record DoctorResponse(
        Long id,
        String name,
        String specialization
) {
    public static DoctorResponse from(Doctor d) {
        return new DoctorResponse(d.getId(), d.getUser().getName(), d.getSpecialization());
    }
}
