package com.hospital.web;

import com.hospital.dto.*;
import com.hospital.security.AppUserDetails;
import com.hospital.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Auth endpoints:
 *   POST /api/auth/register  (admin only)  -> create a user/doctor
 *   POST /api/auth/login                   -> get a JWT
 *   GET  /api/auth/me                      -> current user from the token
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")   // belt-and-suspenders with the SecurityConfig rule
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal AppUserDetails me) {
        return ResponseEntity.ok(UserResponse.from(me.getUser()));
    }

    /** Any logged-in user can change their own password. */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest req,
                                            @AuthenticationPrincipal AppUserDetails me) {
        authService.changePassword(me.getUser(), req);
        return ResponseEntity.ok(Map.of("detail", "Password updated successfully"));
    }
}

