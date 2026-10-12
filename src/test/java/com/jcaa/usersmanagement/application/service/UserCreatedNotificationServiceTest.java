package com.jcaa.usersmanagement.application.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.jcaa.usersmanagement.application.port.out.EmailSenderPort;
import com.jcaa.usersmanagement.application.service.dto.command.NotifyUserCreatedCommand;
import com.jcaa.usersmanagement.domain.exception.EmailSenderException;
import com.jcaa.usersmanagement.domain.exception.InvalidNotificationEventException;
import com.jcaa.usersmanagement.domain.model.EmailDestinationModel;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("UserCreatedNotificationService")
@ExtendWith(MockitoExtension.class)
class UserCreatedNotificationServiceTest {

  private static final String EMAIL = "john@example.com";

  @Mock private EmailSenderPort emailSenderPort;

  private UserCreatedNotificationService service;

  @BeforeEach
  void setUp() {
    final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    service = new UserCreatedNotificationService(emailSenderPort, validator);
  }

  @Test
  @DisplayName("execute() envía correo al destinatario con datos escapados en el cuerpo")
  void shouldSendEmailWithRenderedBody() {
    // Arrange
    final NotifyUserCreatedCommand command =
        new NotifyUserCreatedCommand("u-1", "John <b>", EMAIL, "ADMIN");

    // Act
    service.execute(command);

    // Assert
    final ArgumentCaptor<EmailDestinationModel> captor =
        ArgumentCaptor.forClass(EmailDestinationModel.class);
    verify(emailSenderPort).send(captor.capture());
    assertAll(
        () -> assertEquals(EMAIL, captor.getValue().getDestinationEmail()),
        () -> assertTrue(captor.getValue().getBody().contains("John &lt;b&gt;")),
        () -> assertTrue(captor.getValue().getBody().contains("ADMIN")));
  }

  @Test
  @DisplayName("execute() rechaza evento inválido sin llamar al puerto")
  void shouldRejectInvalidCommand() {
    // Arrange
    final NotifyUserCreatedCommand command =
        new NotifyUserCreatedCommand("u-1", "John", "not-an-email", "ADMIN");

    // Act & Assert
    assertThrows(InvalidNotificationEventException.class, () -> service.execute(command));
    verify(emailSenderPort, never()).send(any());
  }

  @Test
  @DisplayName("execute() propaga EmailSenderException para que Kafka reintente")
  void shouldPropagateSenderFailure() {
    // Arrange
    doThrow(EmailSenderException.becauseDeliveryFailed("503")).when(emailSenderPort).send(any());
    final NotifyUserCreatedCommand command =
        new NotifyUserCreatedCommand("u-1", "John", EMAIL, "ADMIN");

    // Act & Assert
    assertThrows(EmailSenderException.class, () -> service.execute(command));
  }
}
