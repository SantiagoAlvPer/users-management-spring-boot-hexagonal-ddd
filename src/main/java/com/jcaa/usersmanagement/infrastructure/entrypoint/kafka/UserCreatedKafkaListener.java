package com.jcaa.usersmanagement.infrastructure.entrypoint.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcaa.usersmanagement.application.port.in.NotifyUserCreatedUseCase;
import com.jcaa.usersmanagement.application.service.dto.command.NotifyUserCreatedCommand;
import com.jcaa.usersmanagement.domain.exception.InvalidNotificationEventException;
import com.jcaa.usersmanagement.infrastructure.entrypoint.kafka.dto.UserCreatedEventMessage;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCreatedKafkaListener {

  private static final String LOG_RECEIVED =
      "[UserCreatedKafkaListener] evento recibido. topic={} partition={} offset={}";

  private final NotifyUserCreatedUseCase notifyUserCreatedUseCase;
  private final ObjectMapper objectMapper;

  @KafkaListener(topics = "${notifications.kafka.topic.user-created}")
  public void onUserCreated(final ConsumerRecord<String, String> record) {
    log.info(LOG_RECEIVED, record.topic(), record.partition(), record.offset());
    notifyUserCreatedUseCase.execute(toCommand(record.value()));
  }

  private NotifyUserCreatedCommand toCommand(final String json) {
    try {
      final UserCreatedEventMessage message =
          objectMapper.readValue(json, UserCreatedEventMessage.class);
      if (Objects.isNull(message) || Objects.isNull(message.payload())) {
        throw InvalidNotificationEventException.becauseEventIsMissing();
      }
      final UserCreatedEventMessage.Payload payload = message.payload();
      return new NotifyUserCreatedCommand(
          payload.id(), payload.name(), payload.email(), payload.role());
    } catch (final JsonProcessingException | IllegalArgumentException exception) {
      throw InvalidNotificationEventException.becauseEventIsUnreadable(exception);
    }
  }
}
