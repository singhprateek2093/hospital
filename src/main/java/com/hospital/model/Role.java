package com.hospital.model;

/**
 * The three roles in the system. Stored in the DB as a string (see User entity).
 *
 *  ADMIN        - full access: manage users, see/create all patients & intake.
 *  DOCTOR       - sees ONLY patients assigned to them; can record intake for them.
 *  RECEPTIONIST - front desk: register/list all patients and record intake,
 *                 but is not a clinician (kept simple here; extend as needed).
 *
 * Spring Security expects authorities prefixed with "ROLE_", so we expose that here.
 */
public enum Role {
    ADMIN,
    DOCTOR,
    RECEPTIONIST;

    /** e.g. ROLE_ADMIN — the authority string Spring Security's hasRole() checks. */
    public String asAuthority() {
        return "ROLE_" + name();
    }
}
