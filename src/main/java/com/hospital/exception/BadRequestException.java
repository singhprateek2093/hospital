package com.hospital.exception;

/** Thrown for semantically invalid requests (e.g. duplicate email) -> HTTP 400. */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
