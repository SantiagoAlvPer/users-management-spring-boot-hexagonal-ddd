package com.jcaa.usersmanagement.infrastructure.entrypoint.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcaa.usersmanagement.application.port.in.NotifyUserCreatedUseCase;
import com.jcaa.usersmanagement.application.service.dto.command.NotifyUserCreatedCommand;
import com.jcaa.usersmanagement.domain.exception.InvalidNotificationEventException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("UserCreatedKafkaListener")
@ExtendWith(MockitoExtension.class)
class UserCreatedKafkaListenerTest {

  private static final String TOPIC = "user.created";

  @Mock private NotifyUserCreatedUseCase useCase;

  private UserCreatedKafkaListener listener;

  @BeforeEach
  void setUp() {
    listener = new UserCreatedKafkaListener(useCase, new ObjectMapper());
  }

  @Test
  @DisplayName("onUserCreated() mapea el evento JSON a comando y ejecuta el caso de uso")
  void shouldMapEventToCommand() {
    // Arrange
    final String json =
        "{\"eventName\":\"user.created\",\"occurredOn\":\"2026-01-01T10:00:00\","
            + "\"payload\":{\"id\":\"u-1\",\"name\":\"John\",\"email\":\"john@example.com\","
            + "\"role\":\"ADMIN\",\"status\":\"ACTIVE\"}}";

    // Act
    listener.onUserCreated(new ConsumerRecord<>(TOPIC, 0, 0L, "u-1", json));

    // Assert
    final ArgumentCaptor<NotifyUserCreatedCommand> captor =
        ArgumentCaptor.forClass(NotifyUserCreatedCommand.class);
    verify(useCase).execute(captor.capture());
    assertEquals(
        new NotifyUserCreatedCommand("u-1", "John", "john@example.com", "ADMIN"),
        captor.getValue());
  }

  @Test
  @DisplayName("onUserCreated() lanza InvalidNotificationEventException con JSON ilegible")
  void shouldRejectMalformedJson() {
    // Arrange
    final ConsumerRecord<String, String> record = new ConsumerRecord<>(TOPIC, 0, 0L, "k", "{bad");

    // Act & Assert
    assertThrows(InvalidNotificationEventException.class, () -> listener.onUserCreated(record));
    verify(useCase, never()).execute(any());
  }

  @Test
  @DisplayName("onUserCreated() lanza InvalidNotificationEventException sin payload")
  void shouldRejectMissingPayload() {
    // Arrange
    final ConsumerRecord<String, String> record =
        new ConsumerRecord<>(TOPIC, 0, 0L, "k", "{\"eventName\":\"user.created\"}");

    // Act & Assert
    assertThrows(InvalidNotificationEventException.class, () -> listener.onUserCreated(record));
  }
}
