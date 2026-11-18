package com.jcaa.usersmanagement.application.service.dto.query;

public record PresignedDownloadUrl(String key, String url, long expiresInSeconds) {}
