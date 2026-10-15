package com.jcaa.notifiers.infrastructure.adapter.messaging.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcaa.notifiers.application.port.in.SendNotificationUseCase;
import com.jcaa.notifiers.application.service.dto.AccountCreatedNotificationCommand;
import com.jcaa.notifiers.infrastructure.adapter.messaging.kafka.dto.AccountEventKafkaMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountEventsKafkaConsumer {

  private final SendNotificationUseCase sendNotificationUseCase;
  private final ObjectMapper objectMapper;

  @KafkaListener(
      topics = "${kafka.topic.account-created:account.created}",
      groupId = "${spring.kafka.consumer.group-id:notifiers-group}")
  public void consume(final String message) {
    log.info("[AccountEventsKafkaConsumer] Mensaje de cuenta recibido desde Kafka: {}", message);

    try {
      final AccountEventKafkaMessage event =
          objectMapper.readValue(message, AccountEventKafkaMessage.class);
      processEvent(event);
    } catch (final JsonProcessingException ex) {
      log.error(
          "[AccountEventsKafkaConsumer] Error deserializando JSON de cuenta desde Kafka: {}. Mensaje: {}",
          ex.getMessage(),
          message,
          ex);
    } catch (final Exception ex) {
      log.error(
          "[AccountEventsKafkaConsumer] Error procesando notificación de cuenta desde Kafka: {}",
          ex.getMessage(),
          ex);
    }
  }

  void processEvent(final AccountEventKafkaMessage event) {
    final String email = event.getEffectiveEmail();
    if (email == null || email.isBlank()) {
      log.warn(
          "[AccountEventsKafkaConsumer] Se descartó evento de Kafka account.created sin dirección de email (cuenta: {})",
          event.getEffectiveAccountNumber());
      return;
    }

    log.info(
        "[AccountEventsKafkaConsumer] Procesando notificación de cuenta creada para {} (cuenta: {})",
        email,
        event.getEffectiveAccountNumber());

    final AccountCreatedNotificationCommand command =
        new AccountCreatedNotificationCommand(
            event.getEffectiveName(),
            email,
            event.getEffectiveAccountNumber(),
            event.getEffectiveBalance(),
            event.getEffectiveCurrency(),
            event.getEffectiveStatus());

    sendNotificationUseCase.notifyAccountCreated(command);
  }
}
