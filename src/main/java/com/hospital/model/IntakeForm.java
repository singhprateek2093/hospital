package com.hospital.model;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * A single intake / visit record attached to a patient: symptoms, vitals, notes.
 */
@Entity
@Table(name = "intake_forms")
public class IntakeForm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(nullable = false, length = 1000)
    private String symptoms;

    @Column(length = 1000)
    private String vitals;

    @Column(length = 2000)
    private String notes;

    @Column(name = "recorded_at", nullable = false)
    private Instant date;

    protected IntakeForm() {
        // required by JPA
    }

    public IntakeForm(String symptoms, String vitals, String notes) {
        this.symptoms = symptoms;
        this.vitals = vitals;
        this.notes = notes;
        this.date = Instant.now();
    }

    public Long getId() { return id; }
    public Patient getPatient() { return patient; }
    public String getSymptoms() { return symptoms; }
    public String getVitals() { return vitals; }
    public String getNotes() { return notes; }
    public Instant getDate() { return date; }

    public void setPatient(Patient patient) { this.patient = patient; }
}
