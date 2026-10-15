package com.jcaa.accounts.infrastructure.adapter.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcaa.accounts.application.port.out.PublishAccountCreatedEventPort;
import com.jcaa.accounts.domain.event.AccountCreatedDomainEvent;
import com.jcaa.accounts.infrastructure.adapter.messaging.dto.AccountEventMessage;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Paso 7: publica {@code account.created} en Kafka de forma asincrona. Un fallo de Kafka se
 * registra en el log pero no revierte la cuenta ya creada en la base de datos.
 */
@Slf4j
@Component
public class KafkaAccountEventPublisherAdapter implements PublishAccountCreatedEventPort {

  private static final String LOG_SENT =
      "[KafkaAccountEventPublisher] evento publicado. topic={} partition={} offset={} accountId={}";
  private static final String LOG_FAILED =
      "[KafkaAccountEventPublisher] no se pudo publicar el evento. topic={} accountId={} error={}";

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;
  private final String topic;

  public KafkaAccountEventPublisherAdapter(
      final KafkaTemplate<String, String> kafkaTemplate,
      final ObjectMapper objectMapper,
      @Value("${accounts.kafka.topic.account-created}") final String topic) {
    this.kafkaTemplate = kafkaTemplate;
    this.objectMapper = objectMapper;
    this.topic = topic;
  }

  @Override
  public void publish(final AccountCreatedDomainEvent event) {
    final String accountId = event.getAccount().getId().value();
    try {
      final String json = objectMapper.writeValueAsString(toMessage(event));
      // key = accountId: los eventos de una misma cuenta caen en la misma particion
      kafkaTemplate
          .send(topic, accountId, json)
          .whenComplete(
              (result, error) -> {
                if (Objects.nonNull(error)) {
                  log.error(LOG_FAILED, topic, accountId, rootCause(error));
                  return;
                }
                log.info(
                    LOG_SENT,
                    topic,
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset(),
                    accountId);
              });
    } catch (final JsonProcessingException | RuntimeException exception) {
      log.error(LOG_FAILED, topic, accountId, rootCause(exception));
    }
  }

  /** Mensaje de la causa raiz (p.ej. certificado o bootstrap-servers invalidos). */
  private static String rootCause(final Throwable error) {
    final Throwable cause = NestedExceptionUtils.getMostSpecificCause(error);
    return cause.getClass().getSimpleName() + ": " + cause.getMessage();
  }

  private static AccountEventMessage toMessage(final AccountCreatedDomainEvent event) {
    return new AccountEventMessage(
        event.getEventName(), event.getOccurredOn().toString(), event.payload());
  }
}
