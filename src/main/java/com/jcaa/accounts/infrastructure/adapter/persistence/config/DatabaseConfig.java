package com.jcaa.accounts.infrastructure.adapter.persistence.config;

/**
 * Datos de conexion a TiDB/MySQL. TiDB Cloud exige TLS: usar {@code sslMode=VERIFY_IDENTITY}.
 */
public record DatabaseConfig(
    String host, int port, String databaseName, String username, String password, String sslMode) {

  private static final String URL_TEMPLATE =
      "jdbc:mysql://%s:%d/%s?sslMode=%s&enabledTLSProtocols=TLSv1.2,TLSv1.3"
          + "&allowPublicKeyRetrieval=true&serverTimezone=UTC";

  public String buildJdbcUrl() {
    return String.format(URL_TEMPLATE, host, port, databaseName, sslMode);
  }
}
