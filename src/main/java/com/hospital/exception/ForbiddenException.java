package com.hospital.exception;

/**
 * Thrown when an authenticated user is not allowed to access a specific resource
 * (e.g. a doctor requesting another doctor's patient) -> mapped to HTTP 403.
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
