package com.jcaa.notifiers.domain.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("EmailSenderException")
class EmailSenderExceptionTest {

  @Test
  @DisplayName("crea excepcion becauseSendFailed")
  void shouldCreateBecauseSendFailed() {
    final RuntimeException cause = new RuntimeException("connection timeout");
    final EmailSenderException exception = EmailSenderException.becauseSendFailed(cause);

    assertEquals("La notificación por correo no pudo ser enviada.", exception.getMessage());
    assertEquals(cause, exception.getCause());
  }

  @Test
  @DisplayName("crea excepcion becauseApiFailed con detalle")
  void shouldCreateBecauseApiFailed() {
    final EmailSenderException exception =
        EmailSenderException.becauseApiFailed("dest@example.com", "HTTP 500");

    assertNotNull(exception.getMessage());
    assertEquals(
        "No se pudo enviar el correo a 'dest@example.com' mediante la API de Brevo: HTTP 500",
        exception.getMessage());
  }
}
