package com.hospital.repo;

import com.hospital.model.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * All "list" methods take a Pageable and return a Page, giving us pagination +
 * sorting for free (page number, size, total count, etc.).
 */
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Page<Appointment> findByDoctorId(Long doctorId, Pageable pageable);

    Page<Appointment> findByPatientId(Long patientId, Pageable pageable);

    Page<Appointment> findByDoctorIdAndPatientId(Long doctorId, Long patientId, Pageable pageable);
}
