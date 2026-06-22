package com.hospital.repo;

import com.hospital.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    /** Find the Doctor profile that belongs to a given login account. */
    Optional<Doctor> findByUserId(Long userId);
}
