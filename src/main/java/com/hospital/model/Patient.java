package com.hospital.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * A patient record. The single most important field for access control is
 * {@link #assignedDoctor} — that's what lets a doctor see "only their patients".
 */
@Entity
@Table(name = "patients")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int age;

    @Column(nullable = false)
    private String gender;

    @Column(nullable = false)
    private String contact;

    /** The doctor responsible for this patient. May be null (unassigned). */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_doctor_id")
    private Doctor assignedDoctor;

    /** Audit: which user (admin/receptionist) registered this patient. */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /**
     * Visit/intake history. A patient can have many intake forms over time.
     * orphanRemoval keeps things tidy if a form is detached.
     */
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IntakeForm> intakeForms = new ArrayList<>();

    protected Patient() {
        // required by JPA
    }

    public Patient(String name, int age, String gender, String contact,
                   Doctor assignedDoctor, User createdBy) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.contact = contact;
        this.assignedDoctor = assignedDoctor;
        this.createdBy = createdBy;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getGender() { return gender; }
    public String getContact() { return contact; }
    public Doctor getAssignedDoctor() { return assignedDoctor; }
    public User getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public List<IntakeForm> getIntakeForms() { return intakeForms; }

    public void setName(String name) { this.name = name; }
    public void setAge(int age) { this.age = age; }
    public void setGender(String gender) { this.gender = gender; }
    public void setContact(String contact) { this.contact = contact; }
    public void setAssignedDoctor(Doctor assignedDoctor) { this.assignedDoctor = assignedDoctor; }

    /** Keeps both sides of the relationship in sync. */
    public void addIntakeForm(IntakeForm form) {
        intakeForms.add(form);
        form.setPatient(this);
    }
}
