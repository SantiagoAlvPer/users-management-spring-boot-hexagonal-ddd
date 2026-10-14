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
      topics = "#{'${kafka.topics:user.created,user.updated,user-notifications}'.split(',')}",
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
    final String email = event.getEffectiveEmail();
    if (email == null || email.isBlank()) {
      log.warn("[UserEventsKafkaConsumer] Se descartó evento de Kafka sin dirección de email.");
      return;
    }

    final String eventName = event.getEffectiveEventName().toLowerCase();

    if (eventName.contains("updated") || "user_updated".equalsIgnoreCase(eventName)) {
      log.info(
          "[UserEventsKafkaConsumer] Procesando notificación de usuario actualizado para {}",
          email);
      final UserUpdatedNotificationCommand command =
          new UserUpdatedNotificationCommand(
              event.getEffectiveName(),
              email,
              event.getEffectiveRole(),
              event.getEffectiveStatus());
      sendNotificationUseCase.notifyUserUpdated(command);
    } else {
      log.info(
          "[UserEventsKafkaConsumer] Procesando notificación de usuario creado para {}",
          email);
      final String password =
          event.getEffectivePassword() != null ? event.getEffectivePassword() : "";
      final UserCreatedNotificationCommand command =
          new UserCreatedNotificationCommand(
              event.getEffectiveName(),
              email,
              password,
              event.getEffectiveRole());
      sendNotificationUseCase.notifyUserCreated(command);
    }
  }
}
