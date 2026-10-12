package com.jcaa.usersmanagement.infrastructure.config;

import com.jcaa.usersmanagement.domain.exception.InvalidNotificationEventException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

/**
 * Resiliencia del consumidor: reintentos con backoff exponencial ante fallas transitorias de Brevo
 * y envio a {@code <topic>.DLT} al agotarlos. Eventos invalidos van directo al DLT (sin reintento).
 */
@Configuration
public class KafkaErrorHandlingConfig {

  @Bean
  public DefaultErrorHandler kafkaErrorHandler(
      final KafkaTemplate<String, String> kafkaTemplate,
      @Value("${notifications.retry.max-attempts}") final int maxAttempts,
      @Value("${notifications.retry.initial-interval-ms}") final long initialIntervalMs,
      @Value("${notifications.retry.multiplier}") final double multiplier,
      @Value("${notifications.retry.max-interval-ms}") final long maxIntervalMs) {
    final ExponentialBackOff backOff = new ExponentialBackOff(initialIntervalMs, multiplier);
    backOff.setMaxInterval(maxIntervalMs);
    backOff.setMaxAttempts(maxAttempts);
    final DefaultErrorHandler handler =
        new DefaultErrorHandler(new DeadLetterPublishingRecoverer(kafkaTemplate), backOff);
    handler.addNotRetryableExceptions(InvalidNotificationEventException.class);
    return handler;
  }
}
