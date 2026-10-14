package com.jcaa.notifiers.infrastructure.adapter.messaging.kafka;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcaa.notifiers.application.port.in.SendNotificationUseCase;
import com.jcaa.notifiers.application.service.dto.UserCreatedNotificationCommand;
import com.jcaa.notifiers.application.service.dto.UserUpdatedNotificationCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("UserEventsKafkaConsumer")
class UserEventsKafkaConsumerTest {

  private SendNotificationUseCase sendNotificationUseCase;
  private ObjectMapper objectMapper;
  private UserEventsKafkaConsumer consumer;

  @BeforeEach
  void setUp() {
    sendNotificationUseCase = mock(SendNotificationUseCase.class);
    objectMapper = new ObjectMapper();
    consumer = new UserEventsKafkaConsumer(sendNotificationUseCase, objectMapper);
  }

  @Test
  @DisplayName("procesa evento USER_CREATED plano y llama caso de uso con comando correspondiente")
  void shouldProcessUserCreatedEvent() {
    final String json = """
        {
          "eventType": "USER_CREATED",
          "name": "Maria Perez",
          "email": "maria@example.com",
          "password": "Password99!",
          "role": "ADMIN"
        }
        """;

    consumer.consume(json);

    final ArgumentCaptor<UserCreatedNotificationCommand> captor =
        ArgumentCaptor.forClass(UserCreatedNotificationCommand.class);
    verify(sendNotificationUseCase).notifyUserCreated(captor.capture());

    final UserCreatedNotificationCommand command = captor.getValue();
    assertEquals("Maria Perez", command.name());
    assertEquals("maria@example.com", command.email());
    assertEquals("Password99!", command.password());
    assertEquals("ADMIN", command.role());
  }

  @Test
  @DisplayName("procesa evento user.created anidado enviado por microservicio Users")
  void shouldProcessNestedUserCreatedEvent() {
    final String json = """
        {
          "eventName": "user.created",
          "occurredOn": "2026-10-13T20:35:40",
          "payload": {
            "id": "u-123",
            "name": "Maria Perez",
            "email": "maria@example.com",
            "role": "ADMIN",
            "status": "ACTIVE"
          }
        }
        """;

    consumer.consume(json);

    final ArgumentCaptor<UserCreatedNotificationCommand> captor =
        ArgumentCaptor.forClass(UserCreatedNotificationCommand.class);
    verify(sendNotificationUseCase).notifyUserCreated(captor.capture());

    final UserCreatedNotificationCommand command = captor.getValue();
    assertEquals("Maria Perez", command.name());
    assertEquals("maria@example.com", command.email());
    assertEquals("", command.password());
    assertEquals("ADMIN", command.role());
  }

  @Test
  @DisplayName("procesa evento user.updated anidado enviado por microservicio Users")
  void shouldProcessNestedUserUpdatedEvent() {
    final String json = """
        {
          "eventName": "user.updated",
          "occurredOn": "2026-10-13T20:35:40",
          "payload": {
            "id": "u-123",
            "name": "Carlos Gomez",
            "email": "carlos@example.com",
            "role": "MEMBER",
            "status": "BLOCKED"
          }
        }
        """;

    consumer.consume(json);

    final ArgumentCaptor<UserUpdatedNotificationCommand> captor =
        ArgumentCaptor.forClass(UserUpdatedNotificationCommand.class);
    verify(sendNotificationUseCase).notifyUserUpdated(captor.capture());

    final UserUpdatedNotificationCommand command = captor.getValue();
    assertEquals("Carlos Gomez", command.name());
    assertEquals("carlos@example.com", command.email());
    assertEquals("MEMBER", command.role());
    assertEquals("BLOCKED", command.status());
  }

  @Test
  @DisplayName("procesa evento USER_UPDATED y llama caso de uso con comando correspondiente")
  void shouldProcessUserUpdatedEvent() {
    final String json = """
        {
          "eventType": "USER_UPDATED",
          "name": "Carlos Gomez",
          "email": "carlos@example.com",
          "role": "MEMBER",
          "status": "BLOCKED"
        }
        """;

    consumer.consume(json);

    final ArgumentCaptor<UserUpdatedNotificationCommand> captor =
        ArgumentCaptor.forClass(UserUpdatedNotificationCommand.class);
    verify(sendNotificationUseCase).notifyUserUpdated(captor.capture());

    final UserUpdatedNotificationCommand command = captor.getValue();
    assertEquals("Carlos Gomez", command.name());
    assertEquals("carlos@example.com", command.email());
    assertEquals("MEMBER", command.role());
    assertEquals("BLOCKED", command.status());
  }

  @Test
  @DisplayName("ignora mensaje sin correo sin lanzar excepcion")
  void shouldIgnoreMessageWithoutEmail() {
    final String json = """
        {
          "name": "Sin Correo"
        }
        """;

    consumer.consume(json);

    verify(sendNotificationUseCase, never()).notifyUserCreated(any());
    verify(sendNotificationUseCase, never()).notifyUserUpdated(any());
  }

  @Test
  @DisplayName("no falla ante JSON malformado")
  void shouldHandleMalformedJsonGracefully() {
    final String malformed = "{ this is not a valid json }";

    consumer.consume(malformed);

    verify(sendNotificationUseCase, never()).notifyUserCreated(any());
    verify(sendNotificationUseCase, never()).notifyUserUpdated(any());
  }
}
