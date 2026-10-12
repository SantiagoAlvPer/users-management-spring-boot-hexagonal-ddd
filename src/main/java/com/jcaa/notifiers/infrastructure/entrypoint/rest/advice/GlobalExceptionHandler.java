package com.jcaa.notifiers.infrastructure.entrypoint.rest.advice;

import com.jcaa.notifiers.domain.exception.EmailSenderException;
import java.time.Instant;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(EmailSenderException.class)
  public ResponseEntity<Map<String, Object>> handleEmailSenderException(
      final EmailSenderException ex) {
    log.error("[GlobalExceptionHandler] Error de envío de correo: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
        .body(
            Map.of(
                "timestamp", Instant.now().toString(),
                "error", "Email Delivery Failed",
                "message", ex.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidationException(
      final MethodArgumentNotValidException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            Map.of(
                "timestamp", Instant.now().toString(),
                "error", "Validation Error",
                "message", "Datos de solicitud inválidos"));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGenericException(final Exception ex) {
    log.error("[GlobalExceptionHandler] Error no controlado: {}", ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            Map.of(
                "timestamp", Instant.now().toString(),
                "error", "Internal Server Error",
                "message", ex.getMessage()));
  }
}
