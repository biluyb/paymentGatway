package com.safaricom.mpesa.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * GlobalExceptionHandler
 *
 * <p>Centralized exception handling component implementing RFC 7807 (Problem Details for HTTP APIs).
 * Translates domain, security, and validation exceptions into standardized financial error payloads.</p>
 *
 * @author Bililign Yibgeta
 * @since 1.0.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles business rule violations when account balance is insufficient for a debit operation.
     *
     * @param ex the InsufficientFundsException instance
     * @return ProblemDetail with HTTP 400 Bad Request status
     */
    @ExceptionHandler(InsufficientFundsException.class)
    public ProblemDetail handleInsufficientFunds(InsufficientFundsException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Insufficient Funds");
        problem.setType(URI.create("https://developer.safaricom.et/errors/insufficient-funds"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    /**
     * Handles missing database resources (e.g., account or transaction reference not found).
     *
     * @param ex the ResourceNotFoundException instance
     * @return ProblemDetail with HTTP 404 Not Found status
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(ResourceNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Resource Not Found");
        problem.setType(URI.create("https://developer.safaricom.et/errors/not-found"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    /**
     * Handles invalid business arguments passed to the service layer.
     *
     * @param ex the IllegalArgumentException instance
     * @return ProblemDetail with HTTP 400 Bad Request status
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Bad Request");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    /**
     * Intercepts Jakarta Bean Validation errors from incoming request bodies and transforms
     * field errors into a readable map for API consumers.
     *
     * @param ex the MethodArgumentNotValidException caught by Spring MVC
     * @return ProblemDetail with HTTP 400 status and field error breakdown
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        problem.setTitle("Invalid Request Payload");

        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        problem.setProperty("fieldErrors", errors);
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    /**
     * Fallback exception handler for unhandled or unexpected internal errors.
     * Prevents internal stack traces from leaking to client applications.
     *
     * @param ex the uncaught generic Exception
     * @return ProblemDetail with HTTP 500 Internal Server Error status
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An internal error occurred. Please contact M-PESA support.");
        problem.setTitle("Internal Server Error");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}
