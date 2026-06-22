package com.hospital.web;

import com.hospital.exception.BadRequestException;
import com.hospital.exception.ForbiddenException;
import com.hospital.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

/**
 * Turns exceptions thrown anywhere in a controller into the consistent error shape:
 *   { "detail": "..." }            for most errors
 *   { "detail": [ {loc,msg}, ...] } for 422 validation errors
 *
 * This is the single place that defines how failures look to the frontend.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("detail", ex.getMessage()));
    }

    @ExceptionHandler({ForbiddenException.class, AccessDeniedException.class})
    public ResponseEntity<?> handleForbidden(RuntimeException ex) {
        String detail = ex instanceof ForbiddenException ? ex.getMessage() : "Insufficient permissions";
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("detail", detail));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("detail", ex.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("detail", "Incorrect email or password"));
    }

    /** Bean-validation failures (@Valid) -> 422 with per-field details. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(GlobalExceptionHandler::fieldError)
                .toList();
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("detail", errors));
    }

    private static Map<String, String> fieldError(FieldError fe) {
        return Map.of(
                "loc", "body." + fe.getField(),
                "msg", fe.getDefaultMessage() == null ? "invalid value" : fe.getDefaultMessage());
    }
}
