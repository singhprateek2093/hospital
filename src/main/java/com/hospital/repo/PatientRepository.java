package com.hospital.repo;

import com.hospital.model.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    /** All patients assigned to a specific doctor — the doctor's filtered view. */
    Page<Patient> findByAssignedDoctorId(Long doctorId, Pageable pageable);

    /** Case-insensitive name search for the admin/receptionist patient list. */
    Page<Patient> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /** Name search scoped to one doctor's patients. */
    Page<Patient> findByAssignedDoctorIdAndNameContainingIgnoreCase(Long doctorId, String name, Pageable pageable);
}
