package com.jcaa.usersmanagement.infrastructure.adapter.account;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcaa.usersmanagement.application.port.out.CreateAccountPort;
import com.jcaa.usersmanagement.domain.model.UserModel;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Crea la cuenta del usuario llamando al servicio de cuentas (POST /api/accounts). La llamada es
 * asíncrona y no interrumpe el caso de uso: si falla se registra el error técnico y el usuario ya
 * guardado se conserva. No se loguea ningún dato personal.
 */
@Slf4j
@Component
public class HttpAccountClientAdapter implements CreateAccountPort {

  private static final String FIELD_USER_ID = "userId";
  private static final String FIELD_CURRENCY = "currency";
  private static final String LOG_CREATED =
      "[HttpAccountClientAdapter] cuenta creada. status={}";
  private static final String LOG_REJECTED =
      "[HttpAccountClientAdapter] cuenta NO creada. status={}";
  private static final String LOG_FAILED =
      "[HttpAccountClientAdapter] cuenta NO creada. causa={}";

  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;
  private final URI accountsUri;
  private final String currency;
  private final Duration timeout;

  public HttpAccountClientAdapter(
      final ObjectMapper objectMapper,
      @Value("${accounts.api.url}") final String accountsUrl,
      @Value("${accounts.api.default-currency:COP}") final String currency,
      @Value("${accounts.api.timeout-ms:5000}") final long timeoutMs) {
    this.timeout = Duration.ofMillis(timeoutMs);
    this.httpClient = HttpClient.newBuilder().connectTimeout(timeout).build();
    this.objectMapper = objectMapper;
    this.accountsUri = URI.create(accountsUrl);
    this.currency = currency;
  }

  @Override
  public void createAccountFor(final UserModel user) {
    try {
      final String json =
          objectMapper.writeValueAsString(
              Map.of(FIELD_USER_ID, user.getId().value(), FIELD_CURRENCY, currency));
      final HttpRequest request =
          HttpRequest.newBuilder(accountsUri)
              .timeout(timeout)
              .header("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(json))
              .build();
      httpClient
          .sendAsync(request, HttpResponse.BodyHandlers.discarding())
          .whenComplete(
              (response, error) -> {
                if (error != null) {
                  log.error(LOG_FAILED, error.getMessage());
                } else if (response.statusCode() / 100 == 2) {
                  log.info(LOG_CREATED, response.statusCode());
                } else {
                  log.error(LOG_REJECTED, response.statusCode());
                }
              });
    } catch (final JsonProcessingException | RuntimeException exception) {
      log.error(LOG_FAILED, exception.getMessage());
    }
  }
}
