package com.jcaa.usersmanagement.infrastructure.adapter.persistence.config;

import java.util.Objects;

public record DatabaseConfig(
    String host,
    int port,
    String databaseName,
    String username,
    String password,
    String sslMode) {

  private static final String DEFAULT_SSL_MODE = "require";
  private static final String URL_TEMPLATE = "jdbc:postgresql://%s:%d/%s?sslmode=%s";

  public String buildJdbcUrl() {
    final String resolvedSslMode =
        (Objects.isNull(sslMode) || sslMode.isBlank()) ? DEFAULT_SSL_MODE : sslMode;
    return String.format(URL_TEMPLATE, host, port, databaseName, resolvedSslMode);
  }
}
