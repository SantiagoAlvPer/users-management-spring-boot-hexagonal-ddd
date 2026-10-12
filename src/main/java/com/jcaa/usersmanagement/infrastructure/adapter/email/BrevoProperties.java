package com.jcaa.usersmanagement.infrastructure.adapter.email;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "brevo")
public record BrevoProperties(
    String apiUrl,
    String apiKey,
    String senderEmail,
    String senderName,
    int connectTimeoutMs,
    int readTimeoutMs) {}
