package com.jcaa.notifiers.infrastructure.config;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Slf4j
@EnableKafka
@Configuration(proxyBeanMethods = false)
public class KafkaConsumerConfig {

  @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
  private String bootstrapServers;

  @Value("${spring.kafka.consumer.group-id:notifiers-group}")
  private String groupId;

  @Value("${spring.kafka.consumer.auto-offset-reset:earliest}")
  private String autoOffsetReset;

  // Aiven Kafka Security Protocol (PLAINTEXT, SSL, SASL_SSL, SASL_PLAINTEXT)
  @Value("${spring.kafka.properties.security.protocol:PLAINTEXT}")
  private String securityProtocol;

  @Value("${spring.kafka.properties.sasl.mechanism:}")
  private String saslMechanism;

  @Value("${spring.kafka.properties.sasl.jaas.config:}")
  private String saslJaasConfig;

  @Value("${spring.kafka.properties.ssl.truststore.type:}")
  private String sslTruststoreType;

  @Value("${spring.kafka.properties.ssl.truststore.location:}")
  private String sslTruststoreLocation;

  @Value("${spring.kafka.properties.ssl.truststore.password:}")
  private String sslTruststorePassword;

  @Value("${spring.kafka.properties.ssl.keystore.type:}")
  private String sslKeystoreType;

  @Value("${spring.kafka.properties.ssl.keystore.location:}")
  private String sslKeystoreLocation;

  @Value("${spring.kafka.properties.ssl.keystore.password:}")
  private String sslKeystorePassword;

  @Bean
  public ConsumerFactory<String, String> consumerFactory() {
    final Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

    // Configuraciones de seguridad para Aiven Kafka (SSL o SASL_SSL)
    if (securityProtocol != null && !securityProtocol.isBlank() && !"PLAINTEXT".equalsIgnoreCase(securityProtocol)) {
      props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProtocol);
      log.info("[KafkaConsumerConfig] Habilitando protocolo de seguridad Kafka: {}", securityProtocol);

      if (saslMechanism != null && !saslMechanism.isBlank()) {
        props.put("sasl.mechanism", saslMechanism);
      }
      if (saslJaasConfig != null && !saslJaasConfig.isBlank()) {
        props.put("sasl.jaas.config", saslJaasConfig);
      }
      if (sslTruststoreLocation != null && !sslTruststoreLocation.isBlank()) {
        props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, sslTruststoreLocation);
        if (sslTruststorePassword != null && !sslTruststorePassword.isBlank()) {
          props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, sslTruststorePassword);
        }
        if (sslTruststoreType != null && !sslTruststoreType.isBlank()) {
          props.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, sslTruststoreType);
        }
      }
      if (sslKeystoreLocation != null && !sslKeystoreLocation.isBlank()) {
        props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, sslKeystoreLocation);
        if (sslKeystorePassword != null && !sslKeystorePassword.isBlank()) {
          props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, sslKeystorePassword);
        }
        if (sslKeystoreType != null && !sslKeystoreType.isBlank()) {
          props.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, sslKeystoreType);
        }
      }
    }

    return new DefaultKafkaConsumerFactory<>(props);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
      final ConsumerFactory<String, String> consumerFactory) {
    final ConcurrentKafkaListenerContainerFactory<String, String> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory);

    // Manejo de errores resiliente: reintentar 2 veces con intervalo de 1s antes de registrar fallo
    final DefaultErrorHandler errorHandler =
        new DefaultErrorHandler(
            (record, exception) ->
                log.error(
                    "[KafkaConsumerConfig] Error definitivo consumiendo registro offset {} en partición {}: {}",
                    record.offset(),
                    record.partition(),
                    exception.getMessage()),
            new FixedBackOff(1000L, 2L));

    factory.setCommonErrorHandler(errorHandler);
    return factory;
  }
}
