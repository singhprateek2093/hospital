package com.hospital.service;

import com.hospital.dto.DoctorResponse;
import com.hospital.repo.DoctorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;

    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    /** All doctors, for the assignment dropdown in the intake form. */
    @Transactional(readOnly = true)
    public List<DoctorResponse> listAll() {
        return doctorRepository.findAll().stream()
                .map(DoctorResponse::from)
                .toList();
    }
}
