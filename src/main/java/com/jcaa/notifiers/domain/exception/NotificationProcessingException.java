package com.jcaa.notifiers.domain.exception;

public final class NotificationProcessingException extends DomainException {

  public NotificationProcessingException(final String message) {
    super(message);
  }

  public NotificationProcessingException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
