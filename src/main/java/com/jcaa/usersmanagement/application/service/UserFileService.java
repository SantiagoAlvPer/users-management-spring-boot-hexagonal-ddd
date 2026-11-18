package com.jcaa.usersmanagement.application.service;

import com.jcaa.usersmanagement.application.port.in.DeleteUserFileUseCase;
import com.jcaa.usersmanagement.application.port.in.GetUserFileDownloadUrlUseCase;
import com.jcaa.usersmanagement.application.port.in.ListUserFilesUseCase;
import com.jcaa.usersmanagement.application.port.in.UploadUserFileUseCase;
import com.jcaa.usersmanagement.application.port.out.DeleteUserFilePort;
import com.jcaa.usersmanagement.application.port.out.FileStoragePort;
import com.jcaa.usersmanagement.application.port.out.GetUserByIdPort;
import com.jcaa.usersmanagement.application.port.out.GetUserFileByKeyPort;
import com.jcaa.usersmanagement.application.port.out.GetUserFilesByUserIdPort;
import com.jcaa.usersmanagement.application.port.out.SaveUserFilePort;
import com.jcaa.usersmanagement.application.service.dto.command.DeleteUserFileCommand;
import com.jcaa.usersmanagement.application.service.dto.command.UploadUserFileCommand;
import com.jcaa.usersmanagement.application.service.dto.query.GetUserFileDownloadUrlQuery;
import com.jcaa.usersmanagement.application.service.dto.query.PresignedDownloadUrl;
import com.jcaa.usersmanagement.domain.exception.InvalidUserFileException;
import com.jcaa.usersmanagement.domain.exception.UserFileNotFoundException;
import com.jcaa.usersmanagement.domain.exception.UserNotFoundException;
import com.jcaa.usersmanagement.domain.model.UserFileModel;
import com.jcaa.usersmanagement.domain.valueobject.UserId;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserFileService
    implements UploadUserFileUseCase,
        ListUserFilesUseCase,
        GetUserFileDownloadUrlUseCase,
        DeleteUserFileUseCase {

  private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024;
  private static final Set<String> ALLOWED_CONTENT_TYPES =
      Set.of("application/pdf", "image/png", "image/jpeg");

  private final GetUserByIdPort getUserByIdPort;
  private final FileStoragePort fileStoragePort;
  private final SaveUserFilePort saveUserFilePort;
  private final GetUserFilesByUserIdPort getUserFilesByUserIdPort;
  private final GetUserFileByKeyPort getUserFileByKeyPort;
  private final DeleteUserFilePort deleteUserFilePort;
  private final Validator validator;

  @Value("${b2.presigned-url-ttl-seconds:300}")
  private long presignedUrlTtlSeconds = 300L;

  @Override
  public UserFileModel execute(final UploadUserFileCommand command) {
    validateCommand(command);
    ensureUserExists(command.userId());
    validateFileConstraints(command);

    final String sanitizedFilename = sanitizeFilename(command.originalFilename());
    final String key =
        "usuarios/" + command.userId() + "/" + UUID.randomUUID() + "-" + sanitizedFilename;

    fileStoragePort.upload(key, command.content(), command.contentType());

    final UserFileModel model =
        new UserFileModel(
            UUID.randomUUID().toString(),
            command.userId(),
            key,
            command.contentType(),
            command.sizeBytes(),
            Instant.now());

    return saveUserFilePort.save(model);
  }

  @Override
  public List<UserFileModel> execute(final String userId) {
    ensureUserExists(userId);
    return getUserFilesByUserIdPort.getByUserId(userId);
  }

  @Override
  public PresignedDownloadUrl execute(final GetUserFileDownloadUrlQuery query) {
    validateKeyOwnership(query.userId(), query.fileKey());
    getUserFileByKeyPort
        .getByUserIdAndKey(query.userId(), query.fileKey())
        .orElseThrow(() -> UserFileNotFoundException.becauseKeyWasNotFound(query.fileKey()));

    final long ttlSeconds = presignedUrlTtlSeconds > 0 ? presignedUrlTtlSeconds : 300L;
    final String url =
        fileStoragePort.generatePresignedDownloadUrl(
            query.fileKey(), Duration.ofSeconds(ttlSeconds));
    return new PresignedDownloadUrl(query.fileKey(), url, ttlSeconds);
  }

  @Override
  public void execute(final DeleteUserFileCommand command) {
    validateKeyOwnership(command.userId(), command.fileKey());
    getUserFileByKeyPort
        .getByUserIdAndKey(command.userId(), command.fileKey())
        .orElseThrow(() -> UserFileNotFoundException.becauseKeyWasNotFound(command.fileKey()));

    fileStoragePort.delete(command.fileKey());
    deleteUserFilePort.deleteByUserIdAndKey(command.userId(), command.fileKey());
  }

  private void validateCommand(final UploadUserFileCommand command) {
    final Set<ConstraintViolation<UploadUserFileCommand>> violations = validator.validate(command);
    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }
  }

  private void ensureUserExists(final String userId) {
    final UserId id = new UserId(userId);
    getUserByIdPort
        .getById(id)
        .orElseThrow(() -> UserNotFoundException.becauseIdWasNotFound(id.value()));
  }

  private void validateFileConstraints(final UploadUserFileCommand command) {
    if (command.sizeBytes() > MAX_FILE_SIZE_BYTES) {
      throw new InvalidUserFileException(
          "El archivo supera el tamaño máximo permitido de 5 MB.");
    }
    if (!ALLOWED_CONTENT_TYPES.contains(command.contentType())) {
      throw new InvalidUserFileException(
          "Tipo de archivo no permitido. Solo se permiten: PDF, PNG o JPEG.");
    }
  }

  private void validateKeyOwnership(final String userId, final String fileKey) {
    if (fileKey == null || fileKey.isBlank() || fileKey.contains("..")) {
      throw new InvalidUserFileException("La clave del archivo es inválida.");
    }
    final String expectedPrefix = "usuarios/" + userId + "/";
    if (!fileKey.startsWith(expectedPrefix)) {
      throw new InvalidUserFileException(
          "La clave del archivo no pertenece al usuario especificado.");
    }
  }

  private String sanitizeFilename(final String originalFilename) {
    final String base = originalFilename.replace("\\", "/");
    final int lastSlash = base.lastIndexOf('/');
    final String nameOnly = lastSlash >= 0 ? base.substring(lastSlash + 1) : base;
    final String cleaned = nameOnly.replaceAll("[^a-zA-Z0-9._-]", "_");
    return cleaned.isBlank() ? "archivo" : cleaned;
  }
}
