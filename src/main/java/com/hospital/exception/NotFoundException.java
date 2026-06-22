package com.hospital.exception;

/** Thrown when a requested resource doesn't exist -> mapped to HTTP 404. */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
