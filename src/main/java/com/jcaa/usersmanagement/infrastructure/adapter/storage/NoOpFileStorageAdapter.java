package com.jcaa.usersmanagement.infrastructure.adapter.storage;

import com.jcaa.usersmanagement.application.port.out.FileStoragePort;
import com.jcaa.usersmanagement.domain.exception.FileStorageException;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "b2.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpFileStorageAdapter implements FileStoragePort {

  @Override
  public void upload(final String key, final byte[] content, final String contentType) {
    throw new FileStorageException(
        "Backblaze B2 no está habilitado (configure B2_ENABLED=true y credenciales B2_*).");
  }

  @Override
  public String generatePresignedDownloadUrl(final String key, final Duration ttl) {
    throw new FileStorageException(
        "Backblaze B2 no está habilitado (configure B2_ENABLED=true y credenciales B2_*).");
  }

  @Override
  public void delete(final String key) {
    throw new FileStorageException(
        "Backblaze B2 no está habilitado (configure B2_ENABLED=true y credenciales B2_*).");
  }

  @Override
  public void deleteByPrefix(final String prefix) {
    log.debug(
        "[NoOpFileStorageAdapter] Backblaze B2 deshabilitado; se omite limpieza de '{}'.",
        prefix);
  }
}
