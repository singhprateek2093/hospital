package com.hospital.web;

import com.hospital.dto.*;
import com.hospital.security.AppUserDetails;
import com.hospital.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Patient endpoints. Coarse role gates are here via @PreAuthorize; the per-record
 * "doctor sees only their own" rule is enforced inside PatientService.
 */
@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    /** Register a patient — admin or receptionist only. */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<PatientResponse> create(@Valid @RequestBody PatientRequest req,
                                                  @AuthenticationPrincipal AppUserDetails me) {
        PatientResponse created = patientService.create(req, me.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** List patients — same endpoint, role-filtered + paginated (?page=&size=&search=). */
    @GetMapping
    public ResponseEntity<PagedResponse<PatientResponse>> list(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal AppUserDetails me) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        return ResponseEntity.ok(patientService.list(me.getUser(), search, pageable));
    }

    /** Patient detail + intake history — 403 if a doctor requests someone else's patient. */
    @GetMapping("/{id}")
    public ResponseEntity<PatientDetailResponse> get(@PathVariable Long id,
                                                     @AuthenticationPrincipal AppUserDetails me) {
        return ResponseEntity.ok(patientService.getDetail(id, me.getUser()));
    }

    /** Record an intake/visit — admin, receptionist, or the patient's own doctor. */
    @PostMapping("/{id}/intake")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<IntakeResponse> addIntake(@PathVariable Long id,
                                                    @Valid @RequestBody IntakeRequest req,
                                                    @AuthenticationPrincipal AppUserDetails me) {
        IntakeResponse created = patientService.addIntake(id, req, me.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
