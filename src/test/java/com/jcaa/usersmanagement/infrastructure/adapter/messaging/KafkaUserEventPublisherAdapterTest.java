package com.jcaa.usersmanagement.infrastructure.adapter.messaging;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcaa.usersmanagement.domain.enums.UserRole;
import com.jcaa.usersmanagement.domain.enums.UserStatus;
import com.jcaa.usersmanagement.domain.event.UserCreatedDomainEvent;
import com.jcaa.usersmanagement.domain.model.UserModel;
import com.jcaa.usersmanagement.domain.valueobject.UserEmail;
import com.jcaa.usersmanagement.domain.valueobject.UserId;
import com.jcaa.usersmanagement.domain.valueobject.UserName;
import com.jcaa.usersmanagement.domain.valueobject.UserPassword;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

@DisplayName("KafkaUserEventPublisherAdapter")
@ExtendWith(MockitoExtension.class)
class KafkaUserEventPublisherAdapterTest {

  @Mock private KafkaTemplate<String, String> kafkaTemplate;

  private KafkaUserEventPublisherAdapter adapter;
  private UserCreatedDomainEvent event;

  @BeforeEach
  void setUp() {
    adapter = new KafkaUserEventPublisherAdapter(kafkaTemplate, new ObjectMapper(), "");
    event =
        new UserCreatedDomainEvent(
            new UserModel(
                new UserId("u-01"),
                new UserName("John Arrieta"),
                new UserEmail("john@example.com"),
                UserPassword.fromPlainText("Pass1234"),
                UserRole.ADMIN,
                UserStatus.PENDING));
  }

  @Test
  @DisplayName("publish() envía al topic user.created con key=id y JSON sin contraseña")
  void shouldSendEventToTopicWithUserIdAsKey() {
    // Arrange
    when(kafkaTemplate.send(anyString(), anyString(), anyString()))
        .thenReturn(new CompletableFuture<SendResult<String, String>>());

    // Act
    adapter.publish(event);

    // Assert
    final ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
    verify(kafkaTemplate).send(eq("user.created"), eq("u-01"), json.capture());
    assertAll(
        () -> assertTrue(json.getValue().contains("\"eventName\":\"user.created\"")),
        () -> assertTrue(json.getValue().contains("\"email\":\"john@example.com\"")),
        () -> assertFalse(json.getValue().toLowerCase().contains("password")));
  }

  @Test
  @DisplayName("publish() no propaga la excepción cuando Kafka falla")
  void shouldNotThrowWhenKafkaFails() {
    // Arrange
    when(kafkaTemplate.send(anyString(), anyString(), anyString()))
        .thenThrow(new IllegalStateException("broker down"));

    // Act & Assert
    assertDoesNotThrow(() -> adapter.publish(event));
  }

  @Test
  @DisplayName("publish() antepone el prefijo configurado al topic")
  void shouldPrefixTopic() {
    // Arrange
    final KafkaUserEventPublisherAdapter prefixed =
        new KafkaUserEventPublisherAdapter(kafkaTemplate, new ObjectMapper(), "dev.");
    when(kafkaTemplate.send(anyString(), anyString(), anyString()))
        .thenReturn(new CompletableFuture<SendResult<String, String>>());

    // Act
    prefixed.publish(event);

    // Assert
    final ArgumentCaptor<String> topic = ArgumentCaptor.forClass(String.class);
    verify(kafkaTemplate).send(topic.capture(), anyString(), anyString());
    assertEquals("dev.user.created", topic.getValue());
  }
}
