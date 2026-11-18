package com.jcaa.usersmanagement.infrastructure.adapter.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcaa.usersmanagement.application.port.out.UserSearchIndexPort;
import com.jcaa.usersmanagement.application.service.dto.query.UserSearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "opensearch.enabled", havingValue = "true")
public class KafkaUserSearchConsumerAdapter {

  private final UserSearchIndexPort userSearchIndexPort;
  private final ObjectMapper objectMapper;

  @KafkaListener(
      topics = {
        "${events.kafka.topic-prefix:}user.created",
        "${events.kafka.topic-prefix:}user.updated",
        "${events.kafka.topic-prefix:}user.deleted"
      },
      groupId = "${opensearch.kafka.group-id:users-search-indexer}")
  public void onUserEvent(final String message) {
    try {
      final JsonNode root = objectMapper.readTree(message);
      final String eventName = root.path("eventName").asText("");
      final JsonNode payload = root.path("payload");

      if ("user.deleted".equals(eventName)) {
        final String id = payload.path("id").asText("");
        if (!id.isBlank()) {
          userSearchIndexPort.deleteUser(id);
        }
        return;
      }

      if ("user.created".equals(eventName) || "user.updated".equals(eventName)) {
        final String id = payload.path("id").asText("");
        if (!id.isBlank()) {
          userSearchIndexPort.indexUser(
              new UserSearchResult(
                  id,
                  payload.path("name").asText(""),
                  payload.path("email").asText(""),
                  payload.path("role").asText(""),
                  payload.path("status").asText("")));
        }
      }
    } catch (final Exception exception) {
      log.error(
          "[KafkaUserSearchConsumerAdapter] Error procesando evento para OpenSearch: {}",
          exception.getMessage());
    }
  }
}
