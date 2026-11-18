package com.jcaa.usersmanagement.infrastructure.adapter.search;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "opensearch")
public record OpenSearchProperties(boolean enabled, String url, String index) {}
