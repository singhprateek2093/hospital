package com.hospital.service;

import com.hospital.dto.ChangePasswordRequest;
import com.hospital.dto.LoginRequest;
import com.hospital.dto.LoginResponse;
import com.hospital.dto.RegisterRequest;
import com.hospital.dto.UserResponse;
import com.hospital.exception.BadRequestException;
import com.hospital.model.Doctor;
import com.hospital.model.Role;
import com.hospital.model.User;
import com.hospital.repo.DoctorRepository;
import com.hospital.repo.UserRepository;
import com.hospital.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication use-cases: creating accounts and logging in.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       DoctorRepository doctorRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /**
     * Create a new account. Called only by an admin (enforced in the controller).
     * If the new account is a DOCTOR, we also create their clinical Doctor profile.
     */
    @Transactional
    public UserResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new BadRequestException("Email already registered");
        }

        User user = new User(
                req.name(),
                req.email(),
                passwordEncoder.encode(req.password()),  // hash, never store plaintext
                req.role());
        user = userRepository.save(user);

        if (req.role() == Role.DOCTOR) {
            if (req.specialization() == null || req.specialization().isBlank()) {
                throw new BadRequestException("specialization is required for doctors");
            }
            doctorRepository.save(new Doctor(user, req.specialization()));
        }

        return UserResponse.from(user);
    }

    /**
     * Let the currently logged-in user change their own password. We re-check the
     * current password (so a stolen token alone can't change it) before saving the
     * new BCrypt hash.
     */
    @Transactional
    public void changePassword(User currentUser, ChangePasswordRequest req) {
        // Reload a managed copy in this transaction (the principal entity is detached).
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (!passwordEncoder.matches(req.currentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }
        if (passwordEncoder.matches(req.newPassword(), user.getPasswordHash())) {
            throw new BadRequestException("New password must be different from the current one");
        }

        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
    }

    /**
     * Verify credentials and issue a JWT. Throws BadCredentialsException on failure,
     * which the global handler turns into 401.
     */
    public LoginResponse login(LoginRequest req) {
        // Delegates to DaoAuthenticationProvider: loads user by email, checks BCrypt hash.
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password()));

        User user = userRepository.findByEmail(req.email()).orElseThrow();
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        return LoginResponse.bearer(token, user.getRole().name(), user.getId());
    }
}
