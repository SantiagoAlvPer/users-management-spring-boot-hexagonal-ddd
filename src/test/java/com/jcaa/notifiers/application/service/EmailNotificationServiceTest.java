package com.jcaa.notifiers.application.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.jcaa.notifiers.application.port.out.EmailSenderPort;
import com.jcaa.notifiers.application.service.dto.UserCreatedNotificationCommand;
import com.jcaa.notifiers.application.service.dto.UserUpdatedNotificationCommand;
import com.jcaa.notifiers.domain.exception.EmailSenderException;
import com.jcaa.notifiers.domain.model.EmailDestinationModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@DisplayName("EmailNotificationService")
class EmailNotificationServiceTest {

  private EmailSenderPort emailSenderPort;
  private EmailNotificationService service;

  @BeforeEach
  void setUp() {
    emailSenderPort = mock(EmailSenderPort.class);
    service = new EmailNotificationService(emailSenderPort);
  }

  @Test
  @DisplayName("construye y envia correo de creacion de usuario con la plantilla renderizada")
  void shouldSendUserCreatedNotification() {
    final UserCreatedNotificationCommand command =
        new UserCreatedNotificationCommand("Sebastian", "sebas@example.com", "Secret123!", "ADMIN");

    assertDoesNotThrow(() -> service.notifyUserCreated(command));

    final ArgumentCaptor<EmailDestinationModel> captor =
        ArgumentCaptor.forClass(EmailDestinationModel.class);
    verify(emailSenderPort).send(captor.capture());

    final EmailDestinationModel captured = captor.getValue();
    assertEquals("sebas@example.com", captured.getDestinationEmail());
    assertEquals("Sebastian", captured.getDestinationName());
    assertTrue(captured.getBody().contains("Sebastian"));
    assertTrue(captured.getBody().contains("sebas@example.com"));
    assertTrue(captured.getBody().contains("Secret123!"));
    assertTrue(captured.getBody().contains("ADMIN"));
  }

  @Test
  @DisplayName("construye correo con texto por defecto cuando password es nulo o vacio")
  void shouldSendUserCreatedNotificationWithFallbackPassword() {
    final UserCreatedNotificationCommand command =
        new UserCreatedNotificationCommand("Sebastian", "sebas@example.com", null, "ADMIN");

    assertDoesNotThrow(() -> service.notifyUserCreated(command));

    final ArgumentCaptor<EmailDestinationModel> captor =
        ArgumentCaptor.forClass(EmailDestinationModel.class);
    verify(emailSenderPort).send(captor.capture());

    final EmailDestinationModel captured = captor.getValue();
    assertTrue(captured.getBody().contains("Definida durante el registro"));
  }

  @Test
  @DisplayName("construye y envia correo de actualizacion de usuario con la plantilla renderizada")
  void shouldSendUserUpdatedNotification() {
    final UserUpdatedNotificationCommand command =
        new UserUpdatedNotificationCommand("Sebastian", "sebas@example.com", "MEMBER", "ACTIVE");

    assertDoesNotThrow(() -> service.notifyUserUpdated(command));

    final ArgumentCaptor<EmailDestinationModel> captor =
        ArgumentCaptor.forClass(EmailDestinationModel.class);
    verify(emailSenderPort).send(captor.capture());

    final EmailDestinationModel captured = captor.getValue();
    assertEquals("sebas@example.com", captured.getDestinationEmail());
    assertTrue(captured.getBody().contains("MEMBER"));
    assertTrue(captured.getBody().contains("ACTIVE"));
  }

  @Test
  @DisplayName("propaga excepcion si el puerto de envio falla")
  void shouldPropagateExceptionWhenPortFails() {
    doThrow(new EmailSenderException("Error de conexion"))
        .when(emailSenderPort)
        .send(any());

    final UserCreatedNotificationCommand command =
        new UserCreatedNotificationCommand("Sebastian", "sebas@example.com", "Secret123!", "ADMIN");

    assertThrows(EmailSenderException.class, () -> service.notifyUserCreated(command));
  }

  @Test
  @DisplayName("construye y envia correo de creacion de cuenta bancaria con la plantilla renderizada")
  void shouldSendAccountCreatedNotification() {
    final com.jcaa.notifiers.application.service.dto.AccountCreatedNotificationCommand command =
        new com.jcaa.notifiers.application.service.dto.AccountCreatedNotificationCommand(
            "Juanito Perez",
            "juanito@example.com",
            "ACC-123456",
            "1000.00",
            "COP",
            "ACTIVE");

    assertDoesNotThrow(() -> service.notifyAccountCreated(command));

    final ArgumentCaptor<EmailDestinationModel> captor =
        ArgumentCaptor.forClass(EmailDestinationModel.class);
    verify(emailSenderPort).send(captor.capture());

    final EmailDestinationModel captured = captor.getValue();
    assertEquals("juanito@example.com", captured.getDestinationEmail());
    assertEquals("Juanito Perez", captured.getDestinationName());
    assertTrue(captured.getBody().contains("ACC-123456"));
    assertTrue(captured.getBody().contains("1000.00"));
    assertTrue(captured.getBody().contains("COP"));
    assertTrue(captured.getBody().contains("ACTIVE"));
  }
}
