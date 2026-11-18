package com.jcaa.usersmanagement.infrastructure.adapter.persistence.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DatabaseConfigTest {

  private static final String HOST = "db.example.com";
  private static final int PORT = 15425;
  private static final String DATABASE = "crud_usuarios";
  private static final String USERNAME = "avnadmin";
  private static final String PASSWORD = "secret";
  private static final String SSL_MODE = "require";

  @Test
  void shouldBuildJdbcUrlWithConfiguredSslMode() {
    // Arrange
    final DatabaseConfig config =
        new DatabaseConfig(HOST, PORT, DATABASE, USERNAME, PASSWORD, SSL_MODE);

    // Act
    final String jdbcUrl = config.buildJdbcUrl();

    // Assert
    assertThat(jdbcUrl)
        .isEqualTo(
            "jdbc:postgresql://db.example.com:15425/crud_usuarios"
                + "?sslmode=require");
  }

  @Test
  void shouldDefaultSslModeToRequireWhenNullOrBlank() {
    // Arrange
    final DatabaseConfig configWithNull =
        new DatabaseConfig(HOST, PORT, DATABASE, USERNAME, PASSWORD, null);
    final DatabaseConfig configWithBlank =
        new DatabaseConfig(HOST, PORT, DATABASE, USERNAME, PASSWORD, "   ");

    // Act
    final String jdbcUrlNull = configWithNull.buildJdbcUrl();
    final String jdbcUrlBlank = configWithBlank.buildJdbcUrl();

    // Assert
    assertThat(jdbcUrlNull)
        .isEqualTo("jdbc:postgresql://db.example.com:15425/crud_usuarios?sslmode=require");
    assertThat(jdbcUrlBlank)
        .isEqualTo("jdbc:postgresql://db.example.com:15425/crud_usuarios?sslmode=require");
  }
}
