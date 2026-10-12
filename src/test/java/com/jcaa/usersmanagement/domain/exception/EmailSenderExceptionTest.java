package com.jcaa.usersmanagement.domain.exception;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("EmailSenderException")
class EmailSenderExceptionTest {

  @Test
  @DisplayName("becauseDeliveryFailed() incluye el detalle del proveedor")
  void shouldIncludeProviderErrorInMessage() {
    // Arrange
    final String providerError = "401 Unauthorized";

    // Act
    final String message = EmailSenderException.becauseDeliveryFailed(providerError).getMessage();

    // Assert
    assertTrue(message.contains(providerError));
  }

  @Test
  @DisplayName("becauseSendFailed() encapsula la causa y produce mensaje no vacío")
  void shouldWrapCauseAndProduceNonBlankMessage() {
    // Arrange
    final Throwable cause = new RuntimeException("IO error");

    // Act
    final EmailSenderException exception = EmailSenderException.becauseSendFailed(cause);

    // Assert
    assertAll(
        () -> assertSame(cause, exception.getCause()),
        () -> assertFalse(exception.getMessage().isBlank()));
  }
}
