package com.jcaa.usersmanagement.application.port.out;

import java.time.Duration;

public interface FileStoragePort {

  void upload(String key, byte[] content, String contentType);

  String generatePresignedDownloadUrl(String key, Duration ttl);

  void delete(String key);

  void deleteByPrefix(String prefix);
}
