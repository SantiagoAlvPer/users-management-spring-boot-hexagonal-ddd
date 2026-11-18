package com.jcaa.usersmanagement.domain.model;

import java.time.Instant;
import lombok.Value;

@Value
public class UserFileModel {
  String id;
  String userId;
  String fileKey;
  String contentType;
  long sizeBytes;
  Instant createdAt;
}
