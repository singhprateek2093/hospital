package com.hospital.dto;

import com.hospital.model.User;

/** Public view of a user (never includes the password hash). */
public record UserResponse(
        Long id,
        String name,
        String email,
        String role
) {
    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getRole().name());
    }
}
