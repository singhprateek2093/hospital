package com.hospital.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response for POST /api/auth/login.
 * Field names are snake_case in JSON to match the agreed contract.
 */
public record LoginResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("token_type") String tokenType,
        String role,
        @JsonProperty("user_id") Long userId
) {
    public static LoginResponse bearer(String token, String role, Long userId) {
        return new LoginResponse(token, "bearer", role, userId);
    }
}
