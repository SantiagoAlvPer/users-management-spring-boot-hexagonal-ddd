package com.jcaa.accounts.infrastructure.adapter.rest.users;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Conexion con el servicio de Users. Si {@code email} y {@code password} vienen vacios, el GET se
 * hace sin token (para un Users con el endpoint abierto).
 */
@ConfigurationProperties(prefix = "users.api")
public record UsersApiProperties(
    String baseUrl,
    String getUserPath,
    String loginPath,
    String email,
    String password,
    long connectTimeoutMs,
    long readTimeoutMs) {

  public boolean hasCredentials() {
    return email != null && !email.isBlank() && password != null && !password.isBlank();
  }
}
