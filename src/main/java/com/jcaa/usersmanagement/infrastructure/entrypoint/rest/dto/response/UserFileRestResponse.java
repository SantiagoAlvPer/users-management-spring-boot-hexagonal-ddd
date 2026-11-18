package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.response;

import com.jcaa.usersmanagement.domain.model.UserFileModel;
import java.time.Instant;

public record UserFileRestResponse(
    String id,
    String userId,
    String fileKey,
    String contentType,
    long sizeBytes,
    Instant createdAt) {

  public static UserFileRestResponse fromModel(final UserFileModel model) {
    return new UserFileRestResponse(
        model.getId(),
        model.getUserId(),
        model.getFileKey(),
        model.getContentType(),
        model.getSizeBytes(),
        model.getCreatedAt());
  }
}
