package com.jcaa.notifiers.infrastructure.config;

import com.jcaa.notifiers.infrastructure.adapter.email.BrevoConfig;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration(proxyBeanMethods = false)
@Slf4j
public class BrevoSpringConfig {

  @Bean
  public BrevoConfig brevoConfig(
      @Value("${brevo.base-url:https://api.brevo.com}") final String baseUrl,
      @Value("${brevo.local.api-key:${brevo.api-key:${BREVO_API_KEY:${SMTP_PASSWORD:xkeysib-default-placeholder-key}}}}")
          final String apiKey,
      @Value("${brevo.local.from.address:${brevo.from.address:${BREVO_FROM_ADDRESS:${SMTP_FROM_ADDRESS:noreply@example.com}}}}")
          final String fromAddress,
      @Value("${brevo.local.from.name:${brevo.from.name:${BREVO_FROM_NAME:${SMTP_FROM_NAME:Notificaciones}}}}")
          final String fromName,
      @Value("${brevo.timeout.connect:2s}") final Duration connectTimeout,
      @Value("${brevo.timeout.read:5s}") final Duration readTimeout,
      @Value("${brevo.retry.max-attempts:3}") final int retryMaxAttempts,
      @Value("${brevo.retry.initial-delay:500ms}") final Duration retryInitialDelay,
      @Value("${brevo.retry.multiplier:2.0}") final double retryMultiplier,
      @Value("${brevo.circuit-breaker.failure-rate-threshold:50}") final int circuitFailureThreshold,
      @Value("${brevo.circuit-breaker.sliding-window-size:10}") final int circuitSlidingWindowSize,
      @Value("${brevo.circuit-breaker.minimum-calls:5}") final int circuitMinimumCalls,
      @Value("${brevo.circuit-breaker.open-duration:30s}") final Duration circuitOpenDuration) {
    final BrevoConfig config =
        new BrevoConfig(
            baseUrl,
            apiKey,
            fromAddress,
            fromName,
            connectTimeout,
            readTimeout,
            retryMaxAttempts,
            retryInitialDelay,
            retryMultiplier,
            circuitFailureThreshold,
            circuitSlidingWindowSize,
            circuitMinimumCalls,
            circuitOpenDuration);
    log.info(
        "[BrevoSpringConfig] Configuración de Brevo cargada. apiKeyFingerprint={} fromAddress={}",
        fingerprint(apiKey),
        fromAddress);
    return config;
  }

  @Bean
  public RestClient brevoRestClient(final BrevoConfig config) {
    final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(config.connectTimeout());
    requestFactory.setReadTimeout(config.readTimeout());
    return RestClient.builder()
        .baseUrl(config.baseUrl())
        .requestFactory(requestFactory)
        .defaultHeader("api-key", config.apiKey())
        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build();
  }

  private static String fingerprint(final String value) {
    try {
      final byte[] digest =
          MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest, 0, 6);
    } catch (final NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 no esta disponible", exception);
    }
  }
}
