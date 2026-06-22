package com.hospital.service;

import com.hospital.dto.*;
import com.hospital.exception.ForbiddenException;
import com.hospital.exception.NotFoundException;
import com.hospital.model.Doctor;
import com.hospital.model.IntakeForm;
import com.hospital.model.Patient;
import com.hospital.model.Role;
import com.hospital.model.User;
import com.hospital.repo.DoctorRepository;
import com.hospital.repo.IntakeFormRepository;
import com.hospital.repo.PatientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Patient use-cases. This is where the role-based access control actually lives —
 * the rule "a doctor sees only their own patients" is enforced here, on the server,
 * so it can't be bypassed by the frontend.
 */
@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final IntakeFormRepository intakeFormRepository;

    public PatientService(PatientRepository patientRepository,
                          DoctorRepository doctorRepository,
                          IntakeFormRepository intakeFormRepository) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.intakeFormRepository = intakeFormRepository;
    }

    /** Register a new patient. Caller is admin/receptionist (gated in controller). */
    @Transactional
    public PatientResponse create(PatientRequest req, User currentUser) {
        Doctor assigned = null;
        if (req.assignedDoctorId() != null) {
            assigned = doctorRepository.findById(req.assignedDoctorId())
                    .orElseThrow(() -> new NotFoundException("Doctor not found"));
        }
        Patient patient = new Patient(
                req.name(), req.age(), req.gender(), req.contact(), assigned, currentUser);
        patient = patientRepository.save(patient);
        return PatientResponse.from(patient);
    }

    /**
     * Paginated, role-filtered patient list:
     *   ADMIN / RECEPTIONIST -> all patients
     *   DOCTOR               -> only patients assigned to them
     * Optional case-insensitive name search.
     */
    @Transactional(readOnly = true)
    public PagedResponse<PatientResponse> list(User currentUser, String search, Pageable pageable) {
        boolean hasSearch = search != null && !search.isBlank();

        Page<Patient> patients;
        if (currentUser.getRole() == Role.DOCTOR) {
            Long doctorId = currentDoctorId(currentUser);
            patients = hasSearch
                    ? patientRepository.findByAssignedDoctorIdAndNameContainingIgnoreCase(doctorId, search, pageable)
                    : patientRepository.findByAssignedDoctorId(doctorId, pageable);
        } else { // ADMIN or RECEPTIONIST
            patients = hasSearch
                    ? patientRepository.findByNameContainingIgnoreCase(search, pageable)
                    : patientRepository.findAll(pageable);
        }

        return PagedResponse.of(patients, PatientResponse::from);
    }

    /** Full patient detail + intake history, with an ownership check for doctors. */
    @Transactional(readOnly = true)
    public PatientDetailResponse getDetail(Long patientId, User currentUser) {
        Patient patient = loadAuthorizedPatient(patientId, currentUser);
        return PatientDetailResponse.from(patient);
    }

    /** Attach an intake/visit record to a patient (with the same ownership check). */
    @Transactional
    public IntakeResponse addIntake(Long patientId, IntakeRequest req, User currentUser) {
        Patient patient = loadAuthorizedPatient(patientId, currentUser);

        IntakeForm form = new IntakeForm(req.symptoms(), req.vitals(), req.notes());
        patient.addIntakeForm(form);              // keeps both sides of the relationship in sync
        form = intakeFormRepository.save(form);   // insert now so the generated id is populated

        return IntakeResponse.from(form);
    }

    // ---- helpers ---------------------------------------------------------

    /**
     * Loads a patient and verifies the current user is allowed to see it.
     * Doctors may only access patients assigned to them; admin/receptionist see all.
     */
    private Patient loadAuthorizedPatient(Long patientId, User currentUser) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new NotFoundException("Patient not found"));

        if (currentUser.getRole() == Role.DOCTOR) {
            Long doctorId = currentDoctorId(currentUser);
            boolean ownsPatient = patient.getAssignedDoctor() != null
                    && patient.getAssignedDoctor().getId().equals(doctorId);
            if (!ownsPatient) {
                throw new ForbiddenException("Not authorized to view this patient");
            }
        }
        return patient;
    }

    /** The Doctor.id for the logged-in doctor account. */
    private Long currentDoctorId(User currentUser) {
        return doctorRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ForbiddenException("No doctor profile for this account"))
                .getId();
    }
}
