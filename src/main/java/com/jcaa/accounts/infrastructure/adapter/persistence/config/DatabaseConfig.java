package com.jcaa.accounts.infrastructure.adapter.persistence.config;

/**
 * Datos de conexion a PostgreSQL. En Render: {@code sslMode=prefer} con el host interno y {@code
 * require} si se conecta con el host externo.
 */
public record DatabaseConfig(
    String host, int port, String databaseName, String username, String password, String sslMode) {

  private static final String URL_TEMPLATE = "jdbc:postgresql://%s:%d/%s?sslmode=%s";

  public String buildJdbcUrl() {
    return String.format(URL_TEMPLATE, host, port, databaseName, sslMode);
  }
}
