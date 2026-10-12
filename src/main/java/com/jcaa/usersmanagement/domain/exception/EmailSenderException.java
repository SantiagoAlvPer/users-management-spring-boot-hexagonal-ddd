package com.jcaa.usersmanagement.domain.exception;

public final class EmailSenderException extends DomainException {

  private static final String DEFAULT_MESSAGE = "La notificación por correo no pudo ser enviada.";
  private static final String MESSAGE_WITH_DETAIL =
      "No se pudo enviar el correo al proveedor. Detalle: %s";

  public EmailSenderException(final String message) {
    super(message);
  }

  public EmailSenderException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public static EmailSenderException becauseDeliveryFailed(final String providerError) {
    return new EmailSenderException(String.format(MESSAGE_WITH_DETAIL, providerError));
  }

  public static EmailSenderException becauseSendFailed(final Throwable cause) {
    return new EmailSenderException(DEFAULT_MESSAGE, cause);
  }
}
