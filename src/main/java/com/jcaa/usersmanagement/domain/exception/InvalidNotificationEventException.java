package com.jcaa.usersmanagement.domain.exception;

import java.util.List;

public final class InvalidNotificationEventException extends DomainException {

  private static final String MESSAGE_MISSING = "El evento de notificacion es requerido.";
  private static final String MESSAGE_INVALID_FIELDS =
      "El evento de notificacion tiene campos invalidos: %s";
  private static final String MESSAGE_UNREADABLE = "El evento de notificacion no se pudo leer.";

  private InvalidNotificationEventException(final String message) {
    super(message);
  }

  private InvalidNotificationEventException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public static InvalidNotificationEventException becauseEventIsMissing() {
    return new InvalidNotificationEventException(MESSAGE_MISSING);
  }

  public static InvalidNotificationEventException becauseFieldsAreInvalid(
      final List<String> fields) {
    return new InvalidNotificationEventException(String.format(MESSAGE_INVALID_FIELDS, fields));
  }

  public static InvalidNotificationEventException becauseEventIsUnreadable(final Throwable cause) {
    return new InvalidNotificationEventException(MESSAGE_UNREADABLE, cause);
  }
}
