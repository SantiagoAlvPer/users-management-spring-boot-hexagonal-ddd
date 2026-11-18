package com.jcaa.usersmanagement.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jcaa.usersmanagement.application.port.out.DeleteUserFilePort;
import com.jcaa.usersmanagement.application.port.out.FileStoragePort;
import com.jcaa.usersmanagement.application.port.out.GetUserByIdPort;
import com.jcaa.usersmanagement.application.port.out.GetUserFileByKeyPort;
import com.jcaa.usersmanagement.application.port.out.GetUserFilesByUserIdPort;
import com.jcaa.usersmanagement.application.port.out.SaveUserFilePort;
import com.jcaa.usersmanagement.application.service.dto.command.UploadUserFileCommand;
import com.jcaa.usersmanagement.application.service.dto.query.GetUserFileDownloadUrlQuery;
import com.jcaa.usersmanagement.application.service.dto.query.PresignedDownloadUrl;
import com.jcaa.usersmanagement.domain.enums.UserRole;
import com.jcaa.usersmanagement.domain.enums.UserStatus;
import com.jcaa.usersmanagement.domain.exception.InvalidUserFileException;
import com.jcaa.usersmanagement.domain.model.UserFileModel;
import com.jcaa.usersmanagement.domain.model.UserModel;
import com.jcaa.usersmanagement.domain.valueobject.UserEmail;
import com.jcaa.usersmanagement.domain.valueobject.UserId;
import com.jcaa.usersmanagement.domain.valueobject.UserName;
import com.jcaa.usersmanagement.domain.valueobject.UserPassword;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserFileServiceTest {

  @Mock private GetUserByIdPort getUserByIdPort;
  @Mock private FileStoragePort fileStoragePort;
  @Mock private SaveUserFilePort saveUserFilePort;
  @Mock private GetUserFilesByUserIdPort getUserFilesByUserIdPort;
  @Mock private GetUserFileByKeyPort getUserFileByKeyPort;
  @Mock private DeleteUserFilePort deleteUserFilePort;

  private UserFileService service;
  private UserModel existingUser;

  @BeforeEach
  void setUp() {
    final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    service =
        new UserFileService(
            getUserByIdPort,
            fileStoragePort,
            saveUserFilePort,
            getUserFilesByUserIdPort,
            getUserFileByKeyPort,
            deleteUserFilePort,
            validator);

    existingUser =
        new UserModel(
            new UserId("user-1"),
            new UserName("Juan Perez"),
            new UserEmail("juan@example.com"),
            UserPassword.fromHash("$2a$12$abcdefghijklmnopqrstuvwxyz01234567890123456789012345"),
            UserRole.MEMBER,
            UserStatus.ACTIVE);
  }

  @Test
  void shouldUploadPdfFileAndSaveMetadata() {
    // Arrange
    final byte[] content = new byte[] {1, 2, 3};
    final UploadUserFileCommand command =
        new UploadUserFileCommand("user-1", "certificado.pdf", "application/pdf", 3L, content);
    when(getUserByIdPort.getById(new UserId("user-1"))).thenReturn(Optional.of(existingUser));
    when(saveUserFilePort.save(any(UserFileModel.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    final UserFileModel result = service.execute(command);

    // Assert
    assertThat(result.getUserId()).isEqualTo("user-1");
    assertThat(result.getFileKey()).startsWith("usuarios/user-1/");
    assertThat(result.getFileKey()).endsWith("-certificado.pdf");
    verify(fileStoragePort).upload(eq(result.getFileKey()), eq(content), eq("application/pdf"));
    verify(saveUserFilePort).save(any(UserFileModel.class));
  }

  @Test
  void shouldRejectDisallowedContentType() {
    // Arrange
    final UploadUserFileCommand command =
        new UploadUserFileCommand(
            "user-1", "script.sh", "application/x-sh", 10L, new byte[] {1});
    when(getUserByIdPort.getById(new UserId("user-1"))).thenReturn(Optional.of(existingUser));

    // Act & Assert
    assertThatThrownBy(() -> service.execute(command))
        .isInstanceOf(InvalidUserFileException.class);
  }

  @Test
  void shouldGeneratePresignedDownloadUrlForOwnedKey() {
    // Arrange
    final String key = "usuarios/user-1/123-doc.pdf";
    final UserFileModel fileModel =
        new UserFileModel("file-1", "user-1", key, "application/pdf", 100L, Instant.now());
    when(getUserFileByKeyPort.getByUserIdAndKey("user-1", key))
        .thenReturn(Optional.of(fileModel));
    when(fileStoragePort.generatePresignedDownloadUrl(eq(key), any(Duration.class)))
        .thenReturn("https://b2.example.com/signed");

    // Act
    final PresignedDownloadUrl result =
        service.execute(new GetUserFileDownloadUrlQuery("user-1", key));

    // Assert
    assertThat(result.key()).isEqualTo(key);
    assertThat(result.url()).isEqualTo("https://b2.example.com/signed");
  }

  @Test
  void shouldRejectPathTraversalInKey() {
    // Act & Assert
    assertThatThrownBy(
            () ->
                service.execute(
                    new GetUserFileDownloadUrlQuery("user-1", "usuarios/user-1/../other/doc.pdf")))
        .isInstanceOf(InvalidUserFileException.class);
  }
}
