package com.jcaa.usersmanagement.infrastructure.config;

import com.jcaa.usersmanagement.infrastructure.adapter.email.BrevoProperties;
import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(BrevoProperties.class)
public class BrevoSpringConfig {

  @Bean
  public RestClient brevoRestClient(final BrevoProperties properties) {
    final ClientHttpRequestFactorySettings settings =
        ClientHttpRequestFactorySettings.DEFAULTS
            .withConnectTimeout(Duration.ofMillis(properties.connectTimeoutMs()))
            .withReadTimeout(Duration.ofMillis(properties.readTimeoutMs()));
    return RestClient.builder()
        .baseUrl(properties.apiUrl())
        .requestFactory(ClientHttpRequestFactories.get(settings))
        .defaultHeader("accept", MediaType.APPLICATION_JSON_VALUE)
        .build();
  }
}
