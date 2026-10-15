package com.jcaa.accounts.infrastructure.adapter.rest.users;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withUnauthorizedRequest;

import com.jcaa.accounts.domain.valueobject.UserEmail;
import com.jcaa.accounts.domain.valueobject.UserId;
import com.jcaa.accounts.infrastructure.adapter.rest.users.exception.UsersServiceException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

@DisplayName("UsersRestClientAdapter")
class UsersRestClientAdapterTest {

  private static final String BASE_URL = "http://users.test";
  private static final String LOGIN_URL = BASE_URL + "/api/auth/login";
  private static final String USER_JSON =
      "{\"id\":\"u-1\",\"name\":\"John\",\"email\":\"John@Example.com\",\"role\":\"MEMBER\","
          + "\"status\":\"ACTIVE\"}";

  private MockRestServiceServer server;
  private UsersRestClientAdapter adapter;

  private void setUp(final String email, final String password) {
    final UsersApiProperties properties =
        new UsersApiProperties(
            BASE_URL, "/api/users/{id}", "/api/auth/login", email, password, 1000, 1000);
    final RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
    server = MockRestServiceServer.bindTo(builder).build();
    final RestClient client = builder.build();
    adapter =
        new UsersRestClientAdapter(
            client, properties, new UsersApiTokenProvider(client, properties));
  }

  private void expectLogin(final String token) {
    server
        .expect(requestTo(LOGIN_URL))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().json("{\"email\":\"svc@wallet.com\",\"password\":\"Secret123!\"}"))
        .andRespond(
            withSuccess(
                "{\"accessToken\":\"" + token + "\",\"tokenType\":\"Bearer\",\"expiresIn\":3600}",
                MediaType.APPLICATION_JSON));
  }

  @Test
  @DisplayName("hace login, envia el Bearer token y devuelve el email normalizado")
  void shouldLoginAndReturnEmail() {
    // Arrange
    setUp("svc@wallet.com", "Secret123!");
    expectLogin("tok-1");
    server
        .expect(requestTo(BASE_URL + "/api/users/u-1"))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer tok-1"))
        .andRespond(withSuccess(USER_JSON, MediaType.APPLICATION_JSON));

    // Act
    final Optional<UserEmail> email = adapter.getEmailByUserId(new UserId("u-1"));

    // Assert
    assertAll(
        () -> assertTrue(email.isPresent()),
        () -> assertEquals("john@example.com", email.get().value()));
    server.verify();
  }

  @Test
  @DisplayName("sin credenciales hace el GET sin token")
  void shouldCallWithoutTokenWhenNoCredentials() {
    // Arrange
    setUp("", "");
    server
        .expect(requestTo(BASE_URL + "/api/users/u-1"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(USER_JSON, MediaType.APPLICATION_JSON));

    // Act
    final Optional<UserEmail> email = adapter.getEmailByUserId(new UserId("u-1"));

    // Assert
    assertTrue(email.isPresent());
    server.verify();
  }

  @Test
  @DisplayName("404 de Users -> Optional.empty")
  void shouldReturnEmptyWhenUserNotFound() {
    // Arrange
    setUp("", "");
    server
        .expect(requestTo(BASE_URL + "/api/users/u-9"))
        .andRespond(withStatus(HttpStatus.NOT_FOUND));

    // Act
    final Optional<UserEmail> email = adapter.getEmailByUserId(new UserId("u-9"));

    // Assert
    assertTrue(email.isEmpty());
  }

  @Test
  @DisplayName("401 de Users -> nuevo login y un reintento")
  void shouldReloginOnceWhenTokenIsRejected() {
    // Arrange
    setUp("svc@wallet.com", "Secret123!");
    expectLogin("tok-1");
    server
        .expect(requestTo(BASE_URL + "/api/users/u-1"))
        .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer tok-1"))
        .andRespond(withUnauthorizedRequest());
    expectLogin("tok-2");
    server
        .expect(requestTo(BASE_URL + "/api/users/u-1"))
        .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer tok-2"))
        .andRespond(withSuccess(USER_JSON, MediaType.APPLICATION_JSON));

    // Act
    final Optional<UserEmail> email = adapter.getEmailByUserId(new UserId("u-1"));

    // Assert
    assertTrue(email.isPresent());
    server.verify();
  }

  @Test
  @DisplayName("5xx de Users -> UsersServiceException")
  void shouldFailWhenUsersServiceErrors() {
    // Arrange
    setUp("", "");
    server.expect(requestTo(BASE_URL + "/api/users/u-1")).andRespond(withServerError());
    final UserId userId = new UserId("u-1");

    // Act & Assert
    assertThrows(UsersServiceException.class, () -> adapter.getEmailByUserId(userId));
  }

  @Test
  @DisplayName("usuario sin email valido -> UsersServiceException")
  void shouldFailWhenUserHasNoEmail() {
    // Arrange
    setUp("", "");
    server
        .expect(requestTo(BASE_URL + "/api/users/u-1"))
        .andRespond(
            withSuccess("{\"id\":\"u-1\",\"email\":null}", MediaType.APPLICATION_JSON));
    final UserId userId = new UserId("u-1");

    // Act & Assert
    assertThrows(UsersServiceException.class, () -> adapter.getEmailByUserId(userId));
  }
}
