package com.hospital.web;

import com.hospital.dto.AppointmentRequest;
import com.hospital.dto.AppointmentResponse;
import com.hospital.dto.PagedResponse;
import com.hospital.dto.StatusUpdateRequest;
import com.hospital.security.AppUserDetails;
import com.hospital.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /** Book an appointment — admin or receptionist. */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<AppointmentResponse> create(@Valid @RequestBody AppointmentRequest req,
                                                      @AuthenticationPrincipal AppUserDetails me) {
        AppointmentResponse created = appointmentService.create(req, me.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** Paginated, role-filtered list. ?page=&size= and optional ?patientId=. */
    @GetMapping
    public ResponseEntity<PagedResponse<AppointmentResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(name = "patientId", required = false) Long patientId,
            @AuthenticationPrincipal AppUserDetails me) {
        // Sort soonest-first.
        Pageable pageable = PageRequest.of(page, size, Sort.by("scheduledAt").ascending());
        return ResponseEntity.ok(appointmentService.list(me.getUser(), patientId, pageable));
    }

    /** Update status (SCHEDULED/COMPLETED/CANCELLED) — admin, receptionist, or own doctor. */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<AppointmentResponse> updateStatus(@PathVariable Long id,
                                                            @Valid @RequestBody StatusUpdateRequest req,
                                                            @AuthenticationPrincipal AppUserDetails me) {
        return ResponseEntity.ok(appointmentService.updateStatus(id, req.status(), me.getUser()));
    }
}
