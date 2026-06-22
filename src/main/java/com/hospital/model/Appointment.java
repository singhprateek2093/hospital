package com.hospital.model;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * A scheduled visit between a patient and a doctor.
 * Like Patient, the {@link #doctor} field drives the doctor's "only my schedule" view.
 */
@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Column(nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Appointment() {
        // required by JPA
    }

    public Appointment(Patient patient, Doctor doctor, Instant scheduledAt,
                       String reason, User createdBy) {
        this.patient = patient;
        this.doctor = doctor;
        this.scheduledAt = scheduledAt;
        this.reason = reason;
        this.status = AppointmentStatus.SCHEDULED;
        this.createdBy = createdBy;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public Patient getPatient() { return patient; }
    public Doctor getDoctor() { return doctor; }
    public Instant getScheduledAt() { return scheduledAt; }
    public String getReason() { return reason; }
    public AppointmentStatus getStatus() { return status; }
    public User getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }

    public void setStatus(AppointmentStatus status) { this.status = status; }
    public void setScheduledAt(Instant scheduledAt) { this.scheduledAt = scheduledAt; }
    public void setReason(String reason) { this.reason = reason; }
}
