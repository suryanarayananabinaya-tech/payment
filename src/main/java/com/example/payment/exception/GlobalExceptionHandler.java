package com.example.payment.exception;

import com.example.payment.dto.PaymentResponseDTO;
import com.example.payment.model.ErrorResponse;
import com.example.payment.model.PaymentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Business validation failed: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(
                new ErrorResponse("VALIDATION_ERROR", ex.getMessage())
        );
    }

    //  Any unexpected error (IMPORTANT)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex) {

        log.error("Unhandled exception occurred", ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        "INTERNAL_SERVER_ERROR",
                        "Something went wrong. Please try again later."
                ));
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ErrorResponse> handleExternal(ExternalServiceException ex) {
        log.error("External service failure", ex);
        return ResponseEntity.status(503).body(new ErrorResponse("SERVICE_UNAVAILABLE", ex.getMessage()));
    }
}