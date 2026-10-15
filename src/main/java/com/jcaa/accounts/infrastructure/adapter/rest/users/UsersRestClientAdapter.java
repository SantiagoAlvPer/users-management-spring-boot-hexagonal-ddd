package com.jcaa.accounts.infrastructure.adapter.rest.users;

import com.jcaa.accounts.application.port.out.GetUserEmailPort;
import com.jcaa.accounts.domain.exception.InvalidUserEmailException;
import com.jcaa.accounts.domain.valueobject.UserEmail;
import com.jcaa.accounts.domain.valueobject.UserId;
import com.jcaa.accounts.infrastructure.adapter.rest.users.dto.UserApiResponse;
import com.jcaa.accounts.infrastructure.adapter.rest.users.exception.UsersServiceException;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Adaptador de salida: {@code GET /api/users/{id}} en el servicio de Users para leer el correo. */
@Slf4j
@Component
public class UsersRestClientAdapter implements GetUserEmailPort {

  private static final String BEARER_PREFIX = "Bearer ";
  private static final String LOG_NOT_FOUND =
      "[UsersRestClientAdapter] el usuario no existe en Users. userId={}";
  private static final String LOG_RETRY =
      "[UsersRestClientAdapter] token rechazado por Users, se reintenta con uno nuevo. userId={}";

  private final RestClient usersRestClient;
  private final UsersApiProperties properties;
  private final UsersApiTokenProvider tokenProvider;

  public UsersRestClientAdapter(
      final RestClient usersRestClient,
      final UsersApiProperties properties,
      final UsersApiTokenProvider tokenProvider) {
    this.usersRestClient = usersRestClient;
    this.properties = properties;
    this.tokenProvider = tokenProvider;
  }

  @Override
  public Optional<UserEmail> getEmailByUserId(final UserId userId) {
    try {
      return fetchEmail(userId);
    } catch (final HttpClientErrorException.Unauthorized unauthorized) {
      // El token pudo expirar o Users se reinicio con otra clave: un reintento con login nuevo
      log.warn(LOG_RETRY, userId.value());
      tokenProvider.invalidate();
      return fetchEmailOrFail(userId);
    } catch (final HttpClientErrorException.NotFound notFound) {
      log.info(LOG_NOT_FOUND, userId.value());
      return Optional.empty();
    } catch (final RestClientException exception) {
      throw UsersServiceException.becauseRequestFailed(userId.value(), exception);
    }
  }

  private Optional<UserEmail> fetchEmailOrFail(final UserId userId) {
    try {
      return fetchEmail(userId);
    } catch (final HttpClientErrorException.NotFound notFound) {
      log.info(LOG_NOT_FOUND, userId.value());
      return Optional.empty();
    } catch (final RestClientException exception) {
      throw UsersServiceException.becauseRequestFailed(userId.value(), exception);
    }
  }

  private Optional<UserEmail> fetchEmail(final UserId userId) {
    final UserApiResponse user =
        usersRestClient
            .get()
            .uri(properties.getUserPath(), userId.value())
            .headers(
                headers ->
                    tokenProvider
                        .currentToken()
                        .ifPresent(
                            token -> headers.set(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token)))
            // 404 -> HttpClientErrorException.NotFound, 401 -> Unauthorized (manejador por defecto)
            .retrieve()
            .body(UserApiResponse.class);
    if (Objects.isNull(user)) {
      return Optional.empty();
    }
    return Optional.of(toEmail(userId, user));
  }

  private static UserEmail toEmail(final UserId userId, final UserApiResponse user) {
    try {
      return new UserEmail(user.email());
    } catch (final NullPointerException | InvalidUserEmailException exception) {
      throw UsersServiceException.becauseResponseHasNoEmail(userId.value(), exception);
    }
  }
}
