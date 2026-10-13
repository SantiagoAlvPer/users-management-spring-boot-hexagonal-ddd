package com.jcaa.accounts.infrastructure.entrypoint.rest.advice;

import com.jcaa.accounts.domain.exception.AccountAlreadyExistsException;
import com.jcaa.accounts.domain.exception.AccountNotFoundException;
import com.jcaa.accounts.domain.exception.DomainException;
import com.jcaa.accounts.infrastructure.adapter.persistence.exception.PersistenceException;
import com.jcaa.accounts.infrastructure.entrypoint.rest.dto.response.ApiErrorResponse;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final String MESSAGE_PERSISTENCE = "Error de persistencia.";
  private static final String MESSAGE_INTERNAL = "Error interno del servidor.";
  private static final String MESSAGE_UNREADABLE = "El cuerpo de la peticion no es un JSON valido.";
  private static final String MESSAGE_NOT_FOUND = "Recurso no encontrado.";
  private static final String LOG_PERSISTENCE = "[GlobalExceptionHandler] error de persistencia";
  private static final String LOG_UNEXPECTED = "[GlobalExceptionHandler] error inesperado";

  @ExceptionHandler(AccountNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ApiErrorResponse handleAccountNotFound(final AccountNotFoundException exception) {
    return new ApiErrorResponse(HttpStatus.NOT_FOUND.value(), exception.getMessage());
  }

  @ExceptionHandler(AccountAlreadyExistsException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ApiErrorResponse handleAccountAlreadyExists(
      final AccountAlreadyExistsException exception) {
    return new ApiErrorResponse(HttpStatus.CONFLICT.value(), exception.getMessage());
  }

  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiErrorResponse handleConstraintViolation(final ConstraintViolationException exception) {
    final String message =
        exception.getConstraintViolations().stream()
            .map(v -> v.getPropertyPath() + ": " + v.getMessage())
            .sorted()
            .collect(Collectors.joining(", "));
    return new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiErrorResponse handleMethodArgumentNotValid(
      final MethodArgumentNotValidException exception) {
    final String message =
        exception.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .sorted()
            .collect(Collectors.joining(", "));
    return new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiErrorResponse handleUnreadable(final HttpMessageNotReadableException ignored) {
    return new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), MESSAGE_UNREADABLE);
  }

  @ExceptionHandler(NoResourceFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ApiErrorResponse handleNoResource(final NoResourceFoundException ignored) {
    return new ApiErrorResponse(HttpStatus.NOT_FOUND.value(), MESSAGE_NOT_FOUND);
  }

  @ExceptionHandler(DomainException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiErrorResponse handleDomain(final DomainException exception) {
    return new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), exception.getMessage());
  }

  @ExceptionHandler(PersistenceException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ApiErrorResponse handlePersistence(final PersistenceException exception) {
    log.error(LOG_PERSISTENCE, exception);
    return new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), MESSAGE_PERSISTENCE);
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ApiErrorResponse handleGeneral(final Exception exception) {
    log.error(LOG_UNEXPECTED, exception);
    return new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), MESSAGE_INTERNAL);
  }
}
