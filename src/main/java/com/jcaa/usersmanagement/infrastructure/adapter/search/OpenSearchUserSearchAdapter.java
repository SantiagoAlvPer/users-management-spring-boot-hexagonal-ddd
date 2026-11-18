package com.jcaa.usersmanagement.infrastructure.adapter.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcaa.usersmanagement.application.port.out.UserSearchIndexPort;
import com.jcaa.usersmanagement.application.service.dto.query.UserSearchResult;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@ConditionalOnProperty(name = "opensearch.enabled", havingValue = "true")
@EnableConfigurationProperties(OpenSearchProperties.class)
public class OpenSearchUserSearchAdapter implements UserSearchIndexPort {

  private final RestClient restClient;
  private final ObjectMapper objectMapper;
  private final String indexName;
  private final AtomicBoolean indexEnsured = new AtomicBoolean(false);

  public OpenSearchUserSearchAdapter(
      final OpenSearchProperties properties, final ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    this.indexName =
        (properties.index() == null || properties.index().isBlank())
            ? "users"
            : properties.index();

    final URI uri = URI.create(properties.url());
    final String baseUrl =
        uri.getScheme()
            + "://"
            + uri.getHost()
            + (uri.getPort() > 0 ? ":" + uri.getPort() : "");

    final RestClient.Builder builder = RestClient.builder().baseUrl(baseUrl);
    if (uri.getUserInfo() != null && !uri.getUserInfo().isBlank()) {
      final String encoded =
          Base64.getEncoder()
              .encodeToString(uri.getUserInfo().getBytes(StandardCharsets.UTF_8));
      builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoded);
    }
    this.restClient = builder.build();
    log.info(
        "[OpenSearchUserSearchAdapter] Inicializado para índice '{}' en '{}'.",
        this.indexName,
        baseUrl);
  }

  @Override
  public void indexUser(final UserSearchResult document) {
    ensureIndexExists();
    try {
      final Map<String, Object> body =
          Map.of(
              "id", document.id(),
              "name", document.name(),
              "email", document.email(),
              "role", document.role(),
              "status", document.status());
      restClient
          .put()
          .uri("/{index}/_doc/{id}", indexName, document.id())
          .contentType(MediaType.APPLICATION_JSON)
          .body(body)
          .retrieve()
          .toBodilessEntity();
    } catch (final RuntimeException exception) {
      log.error(
          "[OpenSearchUserSearchAdapter] Error indexando usuario {}: {}",
          document.id(),
          exception.getMessage());
    }
  }

  @Override
  public void deleteUser(final String userId) {
    ensureIndexExists();
    try {
      restClient
          .delete()
          .uri("/{index}/_doc/{id}", indexName, userId)
          .retrieve()
          .toBodilessEntity();
    } catch (final HttpClientErrorException.NotFound ignored) {
      // Documento ya no existe en el índice.
    } catch (final RuntimeException exception) {
      log.error(
          "[OpenSearchUserSearchAdapter] Error eliminando usuario {}: {}",
          userId,
          exception.getMessage());
    }
  }

  @Override
  public List<UserSearchResult> search(final String query, final String status) {
    ensureIndexExists();
    final Map<String, Object> requestBody = buildSearchQuery(query, status);
    try {
      final String rawResponse =
          restClient
              .post()
              .uri("/{index}/_search", indexName)
              .contentType(MediaType.APPLICATION_JSON)
              .body(requestBody)
              .retrieve()
              .body(String.class);

      if (rawResponse == null || rawResponse.isBlank()) {
        return List.of();
      }
      final JsonNode root = objectMapper.readTree(rawResponse);
      final JsonNode hits = root.path("hits").path("hits");
      final List<UserSearchResult> results = new ArrayList<>();
      for (final JsonNode hit : hits) {
        final JsonNode source = hit.path("_source");
        results.add(
            new UserSearchResult(
                source.path("id").asText(),
                source.path("name").asText(),
                source.path("email").asText(),
                source.path("role").asText(),
                source.path("status").asText()));
      }
      return results;
    } catch (final Exception exception) {
      log.error(
          "[OpenSearchUserSearchAdapter] Error buscando usuarios en OpenSearch: {}",
          exception.getMessage());
      return List.of();
    }
  }

  private void ensureIndexExists() {
    if (indexEnsured.get()) {
      return;
    }
    try {
      restClient.head().uri("/{index}", indexName).retrieve().toBodilessEntity();
      indexEnsured.set(true);
    } catch (final HttpClientErrorException.NotFound notFound) {
      createIndex();
    } catch (final RuntimeException exception) {
      log.warn(
          "[OpenSearchUserSearchAdapter] No se pudo verificar índice '{}': {}",
          indexName,
          exception.getMessage());
    }
  }

  private void createIndex() {
    final Map<String, Object> settings =
        Map.of("settings", Map.of("index", Map.of("number_of_shards", 1, "number_of_replicas", 0)));
    try {
      restClient
          .put()
          .uri("/{index}", indexName)
          .contentType(MediaType.APPLICATION_JSON)
          .body(settings)
          .retrieve()
          .toBodilessEntity();
      indexEnsured.set(true);
      log.info("[OpenSearchUserSearchAdapter] Índice '{}' creado (1 shard, 0 réplicas).", indexName);
    } catch (final RuntimeException exception) {
      log.warn(
          "[OpenSearchUserSearchAdapter] Error creando índice '{}': {}",
          indexName,
          exception.getMessage());
    }
  }

  private Map<String, Object> buildSearchQuery(final String query, final String status) {
    final List<Map<String, Object>> mustClauses = new ArrayList<>();
    if (query != null && !query.isBlank()) {
      mustClauses.add(
          Map.of(
              "multi_match",
              Map.of(
                  "query", query.trim(),
                  "fields", List.of("name^2", "email"),
                  "fuzziness", "AUTO")));
    } else {
      mustClauses.add(Map.of("match_all", Map.of()));
    }

    final Map<String, Object> boolQuery = new LinkedHashMap<>();
    boolQuery.put("must", mustClauses);

    if (status != null && !status.isBlank()) {
      boolQuery.put("filter", List.of(Map.of("term", Map.of("status.keyword", status.trim()))));
    }

    return Map.of("query", Map.of("bool", boolQuery));
  }
}
