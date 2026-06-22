package com.hospital.service;

import com.hospital.dto.AppointmentRequest;
import com.hospital.dto.AppointmentResponse;
import com.hospital.dto.PagedResponse;
import com.hospital.exception.ForbiddenException;
import com.hospital.exception.NotFoundException;
import com.hospital.model.*;
import com.hospital.repo.AppointmentRepository;
import com.hospital.repo.DoctorRepository;
import com.hospital.repo.PatientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Appointment use-cases. Same access-control philosophy as patients:
 *   ADMIN / RECEPTIONIST -> all appointments
 *   DOCTOR               -> only appointments where they are the doctor
 */
@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientRepository patientRepository,
                              DoctorRepository doctorRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    /** Book an appointment. Caller is admin/receptionist (gated in controller). */
    @Transactional
    public AppointmentResponse create(AppointmentRequest req, User currentUser) {
        Patient patient = patientRepository.findById(req.patientId())
                .orElseThrow(() -> new NotFoundException("Patient not found"));
        Doctor doctor = doctorRepository.findById(req.doctorId())
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        Appointment appt = new Appointment(
                patient, doctor, req.scheduledAt(), req.reason(), currentUser);
        appt = appointmentRepository.save(appt);
        return AppointmentResponse.from(appt);
    }

    /**
     * Paginated, role-filtered list. Optional patientId narrows to one patient.
     */
    @Transactional(readOnly = true)
    public PagedResponse<AppointmentResponse> list(User currentUser, Long patientId, Pageable pageable) {
        Page<Appointment> page;

        if (currentUser.getRole() == Role.DOCTOR) {
            Long doctorId = currentDoctorId(currentUser);
            page = (patientId != null)
                    ? appointmentRepository.findByDoctorIdAndPatientId(doctorId, patientId, pageable)
                    : appointmentRepository.findByDoctorId(doctorId, pageable);
        } else { // ADMIN / RECEPTIONIST
            page = (patientId != null)
                    ? appointmentRepository.findByPatientId(patientId, pageable)
                    : appointmentRepository.findAll(pageable);
        }

        return PagedResponse.of(page, AppointmentResponse::from);
    }

    /** Change an appointment's status (e.g. mark completed/cancelled). */
    @Transactional
    public AppointmentResponse updateStatus(Long id, AppointmentStatus status, User currentUser) {
        Appointment appt = appointmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Appointment not found"));

        // A doctor may only modify their own appointments.
        if (currentUser.getRole() == Role.DOCTOR) {
            Long doctorId = currentDoctorId(currentUser);
            if (!appt.getDoctor().getId().equals(doctorId)) {
                throw new ForbiddenException("Not authorized to modify this appointment");
            }
        }

        appt.setStatus(status);
        return AppointmentResponse.from(appt);
    }

    private Long currentDoctorId(User currentUser) {
        return doctorRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ForbiddenException("No doctor profile for this account"))
                .getId();
    }
}
