package com.jcaa.usersmanagement.infrastructure.adapter.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "b2")
public record BackblazeB2Properties(
    boolean enabled,
    String endpoint,
    String region,
    String bucket,
    String keyId,
    String appKey,
    long presignedUrlTtlSeconds) {}
