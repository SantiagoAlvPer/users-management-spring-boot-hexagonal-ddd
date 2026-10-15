package com.jcaa.accounts.infrastructure.adapter.rest.users;
import org.springframework.beans.factory.annotation.Autowired;
import com.jcaa.accounts.infrastructure.adapter.rest.users.dto.LoginApiRequest;
import com.jcaa.accounts.infrastructure.adapter.rest.users.dto.LoginApiResponse;
import com.jcaa.accounts.infrastructure.adapter.rest.users.exception.UsersServiceException;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Obtiene y guarda en memoria el JWT de la cuenta de servicio con la que accounts consulta a Users.
 * Renueva el token antes de que expire.
 */
@Slf4j
@Component
public class UsersApiTokenProvider {

  private static final long EXPIRY_MARGIN_SECONDS = 30;
  private static final String LOG_LOGIN = "[UsersApiTokenProvider] token obtenido. expiresIn={}s";

  private final RestClient usersRestClient;
  private final UsersApiProperties properties;
  private final Clock clock;

  private String cachedToken;
  private Instant expiresAt = Instant.EPOCH;

  @Autowired
  public UsersApiTokenProvider(
      final RestClient usersRestClient, final UsersApiProperties properties) {
    this(usersRestClient, properties, Clock.systemUTC());
  }

  UsersApiTokenProvider(
      final RestClient usersRestClient, final UsersApiProperties properties, final Clock clock) {
    this.usersRestClient = usersRestClient;
    this.properties = properties;
    this.clock = clock;
  }

  /** Token vigente, o vacio si no hay credenciales configuradas. */
  public synchronized Optional<String> currentToken() {
    if (!properties.hasCredentials()) {
      return Optional.empty();
    }
    if (Objects.isNull(cachedToken) || !clock.instant().isBefore(expiresAt)) {
      login();
    }
    return Optional.of(cachedToken);
  }

  /** Descarta el token (p.ej. tras un 401) para forzar un nuevo login. */
  public synchronized void invalidate() {
    cachedToken = null;
    expiresAt = Instant.EPOCH;
  }

  private void login() {
    try {
      final LoginApiResponse response =
          usersRestClient
              .post()
              .uri(properties.loginPath())
              .contentType(MediaType.APPLICATION_JSON)
              .body(new LoginApiRequest(properties.email(), properties.password()))
              .retrieve()
              .body(LoginApiResponse.class);
      if (Objects.isNull(response)
          || Objects.isNull(response.accessToken())
          || response.accessToken().isBlank()) {
        throw new IllegalStateException("Empty access token");
      }
      cachedToken = response.accessToken();
      expiresAt =
          clock.instant().plusSeconds(Math.max(0, response.expiresIn() - EXPIRY_MARGIN_SECONDS));
      log.info(LOG_LOGIN, response.expiresIn());
    } catch (final RestClientException | IllegalStateException exception) {
      invalidate();
      throw UsersServiceException.becauseLoginFailed(exception);
    }
  }
}
