package com.jcaa.accounts.infrastructure.adapter.rest.users.exception;

/** Falla al hablar con el servicio de Users (caido, timeout, credenciales o respuesta invalida). */
public final class UsersServiceException extends RuntimeException {

  private static final String MESSAGE_REQUEST =
      "Failed to get user '%s' from the users service.";
  private static final String MESSAGE_LOGIN = "Failed to authenticate against the users service.";
  private static final String MESSAGE_NO_EMAIL =
      "The users service returned user '%s' without a valid email.";

  private UsersServiceException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public static UsersServiceException becauseRequestFailed(
      final String userId, final Throwable cause) {
    return new UsersServiceException(String.format(MESSAGE_REQUEST, userId), cause);
  }

  public static UsersServiceException becauseLoginFailed(final Throwable cause) {
    return new UsersServiceException(MESSAGE_LOGIN, cause);
  }

  public static UsersServiceException becauseResponseHasNoEmail(
      final String userId, final Throwable cause) {
    return new UsersServiceException(String.format(MESSAGE_NO_EMAIL, userId), cause);
  }
}
