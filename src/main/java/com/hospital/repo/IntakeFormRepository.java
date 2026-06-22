package com.hospital.repo;

import com.hospital.model.IntakeForm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntakeFormRepository extends JpaRepository<IntakeForm, Long> {
}
