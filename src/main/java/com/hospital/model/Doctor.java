package com.hospital.model;

import jakarta.persistence.*;

/**
 * Clinical profile for a User whose role is DOCTOR.
 * One User <-> one Doctor. We separate them so the login account (User)
 * stays generic while clinician-specific fields live here.
 */
@Entity
@Table(name = "doctors")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The backing login account. We eagerly need the name/email when listing
     * doctors, but JPA defaults OneToOne to EAGER which is fine here.
     */
    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String specialization;

    protected Doctor() {
        // required by JPA
    }

    public Doctor(User user, String specialization) {
        this.user = user;
        this.specialization = specialization;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getSpecialization() { return specialization; }

    public void setSpecialization(String specialization) { this.specialization = specialization; }
}
