package com.jcaa.usersmanagement.infrastructure.adapter.storage;

import com.jcaa.usersmanagement.application.port.out.FileStoragePort;
import com.jcaa.usersmanagement.domain.exception.FileStorageException;
import jakarta.annotation.PreDestroy;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Slf4j
@Component
@ConditionalOnProperty(name = "b2.enabled", havingValue = "true")
@EnableConfigurationProperties(BackblazeB2Properties.class)
public class BackblazeB2StorageAdapter implements FileStoragePort {

  private final BackblazeB2Properties properties;
  private final S3Client s3Client;
  private final S3Presigner s3Presigner;

  public BackblazeB2StorageAdapter(final BackblazeB2Properties properties) {
    this.properties = properties;
    final StaticCredentialsProvider credentialsProvider =
        StaticCredentialsProvider.create(
            AwsBasicCredentials.create(properties.keyId(), properties.appKey()));
    final URI endpointUri = URI.create(properties.endpoint());
    final Region region = Region.of(properties.region());

    this.s3Client =
        S3Client.builder()
            .endpointOverride(endpointUri)
            .region(region)
            .credentialsProvider(credentialsProvider)
            .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
            .build();

    this.s3Presigner =
        S3Presigner.builder()
            .endpointOverride(endpointUri)
            .region(region)
            .credentialsProvider(credentialsProvider)
            .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
            .build();

    log.info(
        "[BackblazeB2StorageAdapter] Inicializado para bucket '{}' en '{}'.",
        properties.bucket(),
        properties.endpoint());
  }

  @Override
  public void upload(final String key, final byte[] content, final String contentType) {
    try {
      final PutObjectRequest request =
          PutObjectRequest.builder()
              .bucket(properties.bucket())
              .key(key)
              .contentType(contentType)
              .build();
      s3Client.putObject(request, RequestBody.fromBytes(content));
    } catch (final RuntimeException exception) {
      throw new FileStorageException("Error al subir archivo a Backblaze B2.", exception);
    }
  }

  @Override
  public String generatePresignedDownloadUrl(final String key, final Duration ttl) {
    try {
      final GetObjectRequest getObjectRequest =
          GetObjectRequest.builder().bucket(properties.bucket()).key(key).build();
      final GetObjectPresignRequest presignRequest =
          GetObjectPresignRequest.builder()
              .signatureDuration(ttl)
              .getObjectRequest(getObjectRequest)
              .build();
      return s3Presigner.presignGetObject(presignRequest).url().toString();
    } catch (final RuntimeException exception) {
      throw new FileStorageException(
          "Error al generar URL firmada en Backblaze B2.", exception);
    }
  }

  @Override
  public void delete(final String key) {
    try {
      final DeleteObjectRequest request =
          DeleteObjectRequest.builder().bucket(properties.bucket()).key(key).build();
      s3Client.deleteObject(request);
    } catch (final RuntimeException exception) {
      throw new FileStorageException("Error al eliminar archivo en Backblaze B2.", exception);
    }
  }

  @Override
  public void deleteByPrefix(final String prefix) {
    try {
      final ListObjectsV2Request listRequest =
          ListObjectsV2Request.builder().bucket(properties.bucket()).prefix(prefix).build();
      final ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);
      final List<S3Object> objects = listResponse.contents();
      for (final S3Object obj : objects) {
        delete(obj.key());
      }
    } catch (final RuntimeException exception) {
      log.warn(
          "[BackblazeB2StorageAdapter] No se pudieron eliminar archivos con prefijo '{}': {}",
          prefix,
          exception.getMessage());
    }
  }

  @PreDestroy
  void close() {
    s3Presigner.close();
    s3Client.close();
  }
}
