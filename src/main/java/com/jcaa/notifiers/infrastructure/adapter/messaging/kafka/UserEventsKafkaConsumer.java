package com.jcaa.notifiers.infrastructure.adapter.messaging.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcaa.notifiers.application.port.in.SendNotificationUseCase;
import com.jcaa.notifiers.application.service.dto.UserCreatedNotificationCommand;
import com.jcaa.notifiers.application.service.dto.UserUpdatedNotificationCommand;
import com.jcaa.notifiers.infrastructure.adapter.messaging.kafka.dto.UserEventKafkaMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventsKafkaConsumer {

  private final SendNotificationUseCase sendNotificationUseCase;
  private final ObjectMapper objectMapper;

  @KafkaListener(
      topics = "${kafka.topic.user-notifications:user-notifications}",
      groupId = "${spring.kafka.consumer.group-id:notifiers-group}")
  public void consume(final String message) {
    log.info("[UserEventsKafkaConsumer] Mensaje nuevo de email recibido desde Kafka: {}", message);

    try {
      final UserEventKafkaMessage event =
          objectMapper.readValue(message, UserEventKafkaMessage.class);
      processEvent(event);
    } catch (final JsonProcessingException ex) {
      log.error(
          "[UserEventsKafkaConsumer] Error deserializando JSON de Kafka: {}. Mensaje recibido: {}",
          ex.getMessage(),
          message,
          ex);
    } catch (final Exception ex) {
      log.error(
          "[UserEventsKafkaConsumer] Error procesando notificación desde Kafka: {}",
          ex.getMessage(),
          ex);
    }
  }

  void processEvent(final UserEventKafkaMessage event) {
    if (event.getEmail() == null || event.getEmail().isBlank()) {
      log.warn("[UserEventsKafkaConsumer] Se descartó evento de Kafka sin dirección de email.");
      return;
    }

    final String eventType = event.getEventType() != null ? event.getEventType().toUpperCase() : "";

    if ("USER_UPDATED".equals(eventType) || (event.getStatus() != null && event.getPassword() == null)) {
      log.info(
          "[UserEventsKafkaConsumer] Procesando notificación de usuario actualizado para {}",
          event.getEmail());
      final UserUpdatedNotificationCommand command =
          new UserUpdatedNotificationCommand(
              event.getName() != null ? event.getName() : "Usuario",
              event.getEmail(),
              event.getRole() != null ? event.getRole() : "MEMBER",
              event.getStatus() != null ? event.getStatus() : "ACTIVE");
      sendNotificationUseCase.notifyUserUpdated(command);
    } else {
      // Por defecto tratamos como USER_CREATED si viene contraseña o eventType no es UPDATE
      log.info(
          "[UserEventsKafkaConsumer] Procesando notificación de usuario creado para {}",
          event.getEmail());
      final UserCreatedNotificationCommand command =
          new UserCreatedNotificationCommand(
              event.getName() != null ? event.getName() : "Usuario",
              event.getEmail(),
              event.getPassword() != null ? event.getPassword() : "",
              event.getRole() != null ? event.getRole() : "MEMBER");
      sendNotificationUseCase.notifyUserCreated(command);
    }
  }
}
