package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.response;

import com.jcaa.usersmanagement.application.service.dto.query.PresignedDownloadUrl;

public record FileDownloadUrlRestResponse(String key, String url, long expiresInSeconds) {

  public static FileDownloadUrlRestResponse fromQuery(final PresignedDownloadUrl dto) {
    return new FileDownloadUrlRestResponse(dto.key(), dto.url(), dto.expiresInSeconds());
  }
}
