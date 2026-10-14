package com.jcaa.usersmanagement.infrastructure.adapter.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcaa.usersmanagement.application.port.out.PublishUserEventPort;
import com.jcaa.usersmanagement.domain.event.DomainEvent;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publica eventos de dominio de usuario en Kafka. El topic es el nombre del evento (p.ej. {@code
 * user.created}) y la key es el id del usuario, para conservar el orden por usuario.
 *
 * <p>La publicación no interrumpe el caso de uso: si Kafka falla se registra el error técnico y el
 * usuario ya guardado se conserva. No se loguea ningún dato personal.
 */
@Slf4j
@Component
public class KafkaUserEventPublisherAdapter implements PublishUserEventPort {

  private static final String KEY_ID = "id";
  private static final String FIELD_EVENT_NAME = "eventName";
  private static final String FIELD_OCCURRED_ON = "occurredOn";
  private static final String FIELD_PAYLOAD = "payload";
  private static final String LOG_PUBLISHED =
      "[KafkaUserEventPublisherAdapter] evento publicado. event={} topic={} partition={} offset={}";
  private static final String LOG_FAILED =
      "[KafkaUserEventPublisherAdapter] evento NO publicado. event={} causa={}";

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;
  private final String topicPrefix;

  public KafkaUserEventPublisherAdapter(
      final KafkaTemplate<String, String> kafkaTemplate,
      final ObjectMapper objectMapper,
      @Value("${events.kafka.topic-prefix:}") final String topicPrefix) {
    this.kafkaTemplate = kafkaTemplate;
    this.objectMapper = objectMapper;
    this.topicPrefix = topicPrefix;
  }

  @Override
  public void publish(final DomainEvent event) {
    final String topic = topicPrefix + event.getEventName();
    try {
      final String json = serialize(event);
      final String key = event.payload().get(KEY_ID);
      kafkaTemplate
          .send(topic, key, json)
          .whenComplete(
              (result, error) -> {
                if (error != null) {
                  log.error(LOG_FAILED, event.getEventName(), error.getMessage());
                } else {
                  log.info(
                      LOG_PUBLISHED,
                      event.getEventName(),
                      topic,
                      result.getRecordMetadata().partition(),
                      result.getRecordMetadata().offset());
                }
              });
    } catch (final JsonProcessingException | RuntimeException exception) {
      log.error(LOG_FAILED, event.getEventName(), exception.getMessage());
    }
  }

  private String serialize(final DomainEvent event) throws JsonProcessingException {
    return objectMapper.writeValueAsString(
        Map.of(
            FIELD_EVENT_NAME, event.getEventName(),
            FIELD_OCCURRED_ON,
                DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(event.getOccurredOn()),
            FIELD_PAYLOAD, event.payload()));
  }
}
