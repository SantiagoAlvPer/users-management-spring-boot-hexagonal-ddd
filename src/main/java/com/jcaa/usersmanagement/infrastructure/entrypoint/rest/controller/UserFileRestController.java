package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.controller;

import com.jcaa.usersmanagement.application.port.in.DeleteUserFileUseCase;
import com.jcaa.usersmanagement.application.port.in.GetUserFileDownloadUrlUseCase;
import com.jcaa.usersmanagement.application.port.in.ListUserFilesUseCase;
import com.jcaa.usersmanagement.application.port.in.UploadUserFileUseCase;
import com.jcaa.usersmanagement.application.service.dto.command.DeleteUserFileCommand;
import com.jcaa.usersmanagement.application.service.dto.command.UploadUserFileCommand;
import com.jcaa.usersmanagement.application.service.dto.query.GetUserFileDownloadUrlQuery;
import com.jcaa.usersmanagement.application.service.dto.query.PresignedDownloadUrl;
import com.jcaa.usersmanagement.domain.enums.UserRole;
import com.jcaa.usersmanagement.domain.exception.InvalidUserFileException;
import com.jcaa.usersmanagement.domain.model.UserFileModel;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.response.FileDownloadUrlRestResponse;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.response.UserFileRestResponse;
import com.jcaa.usersmanagement.infrastructure.security.JwtPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users/{id}/files")
@RequiredArgsConstructor
@Tag(name = "Archivos de Usuario", description = "Gestión de archivos privados en Backblaze B2")
public class UserFileRestController {

  private final UploadUserFileUseCase uploadUserFileUseCase;
  private final ListUserFilesUseCase listUserFilesUseCase;
  private final GetUserFileDownloadUrlUseCase getUserFileDownloadUrlUseCase;
  private final DeleteUserFileUseCase deleteUserFileUseCase;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Subir archivo privado del usuario")
  public UserFileRestResponse upload(
      @PathVariable final String id,
      @RequestParam("file") final MultipartFile file,
      @AuthenticationPrincipal final JwtPrincipal principal) {
    ensureOwnerOrAdmin(id, principal);
    if (file == null || file.isEmpty()) {
      throw new InvalidUserFileException("El archivo es obligatorio y no puede estar vacío.");
    }
    final byte[] bytes;
    try {
      bytes = file.getBytes();
    } catch (final IOException exception) {
      throw new InvalidUserFileException("No se pudo leer el contenido del archivo.");
    }
    final String filename =
        file.getOriginalFilename() != null ? file.getOriginalFilename() : "archivo";
    final String contentType =
        file.getContentType() != null ? file.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;

    final UserFileModel saved =
        uploadUserFileUseCase.execute(
            new UploadUserFileCommand(id, filename, contentType, file.getSize(), bytes));
    return UserFileRestResponse.fromModel(saved);
  }

  @GetMapping
  @Operation(summary = "Listar archivos del usuario")
  public List<UserFileRestResponse> list(
      @PathVariable final String id,
      @AuthenticationPrincipal final JwtPrincipal principal) {
    ensureOwnerOrAdmin(id, principal);
    return listUserFilesUseCase.execute(id).stream().map(UserFileRestResponse::fromModel).toList();
  }

  @GetMapping("/download")
  @Operation(summary = "Obtener URL firmada temporal de descarga")
  public FileDownloadUrlRestResponse downloadUrl(
      @PathVariable final String id,
      @RequestParam("key") final String key,
      @AuthenticationPrincipal final JwtPrincipal principal) {
    ensureOwnerOrAdmin(id, principal);
    final PresignedDownloadUrl dto =
        getUserFileDownloadUrlUseCase.execute(new GetUserFileDownloadUrlQuery(id, key));
    return FileDownloadUrlRestResponse.fromQuery(dto);
  }

  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Eliminar archivo del usuario")
  public void delete(
      @PathVariable final String id,
      @RequestParam("key") final String key,
      @AuthenticationPrincipal final JwtPrincipal principal) {
    ensureOwnerOrAdmin(id, principal);
    deleteUserFileUseCase.execute(new DeleteUserFileCommand(id, key));
  }

  private void ensureOwnerOrAdmin(final String targetUserId, final JwtPrincipal principal) {
    if (principal == null) {
      throw new AccessDeniedException("Se requiere autenticación.");
    }
    final boolean isAdmin = principal.role() == UserRole.ADMIN;
    final boolean isOwner = targetUserId.equals(principal.userId());
    if (!isAdmin && !isOwner) {
      throw new AccessDeniedException("No tienes permiso para acceder a los archivos de este usuario.");
    }
  }
}
