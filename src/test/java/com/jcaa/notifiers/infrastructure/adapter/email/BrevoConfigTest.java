package com.jcaa.notifiers.infrastructure.adapter.email;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("BrevoConfig")
class BrevoConfigTest {

  private static final String VALID_KEY = "xkeysib-abc123def456";
  private static final String VALID_EMAIL = "noreply@example.com";
  private static final String VALID_URL = "https://api.brevo.com";

  @Test
  @DisplayName("acepta valores validos")
  void shouldAcceptValidConfiguration() {
    assertDoesNotThrow(
        () ->
            new BrevoConfig(
                VALID_URL,
                VALID_KEY,
                VALID_EMAIL,
                "Notifiers",
                Duration.ofSeconds(2),
                Duration.ofSeconds(5),
                3,
                Duration.ofMillis(500),
                2.0,
                50,
                10,
                5,
                Duration.ofSeconds(30)));
  }

  @Test
  @DisplayName("rechaza apiKey sin prefijo xkeysib-")
  void shouldRejectInvalidApiKeyPrefix() {
    final IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                new BrevoConfig(
                    VALID_URL,
                    "invalid-key-without-prefix",
                    VALID_EMAIL,
                    "Notifiers",
                    Duration.ofSeconds(2),
                    Duration.ofSeconds(5),
                    3,
                    Duration.ofMillis(500),
                    2.0,
                    50,
                    10,
                    5,
                    Duration.ofSeconds(30)));

    assertTrue(exception.getMessage().contains("xkeysib-"));
  }

  @Test
  @DisplayName("rechaza remitente vacio")
  void shouldRejectEmptyFromAddress() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new BrevoConfig(
                VALID_URL,
                VALID_KEY,
                "   ",
                "Notifiers",
                Duration.ofSeconds(2),
                Duration.ofSeconds(5),
                3,
                Duration.ofMillis(500),
                2.0,
                50,
                10,
                5,
                Duration.ofSeconds(30)));
  }
}
