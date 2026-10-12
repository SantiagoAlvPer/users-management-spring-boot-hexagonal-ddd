package com.jcaa.notifiers.application.service;

import com.jcaa.notifiers.application.port.in.SendNotificationUseCase;
import com.jcaa.notifiers.application.port.out.EmailSenderPort;
import com.jcaa.notifiers.application.service.dto.UserCreatedNotificationCommand;
import com.jcaa.notifiers.application.service.dto.UserUpdatedNotificationCommand;
import com.jcaa.notifiers.domain.exception.EmailSenderException;
import com.jcaa.notifiers.domain.model.EmailDestinationModel;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationService implements SendNotificationUseCase {

  private static final String SUBJECT_CREATED = "Tu cuenta ha sido creada — Gestion de Usuarios";
  private static final String SUBJECT_UPDATED = "Tu cuenta ha sido actualizada — Gestion de Usuarios";

  private static final String TOKEN_NAME = "name";
  private static final String TOKEN_EMAIL = "email";
  private static final String TOKEN_PASSWORD = "password";
  private static final String TOKEN_ROLE = "role";
  private static final String TOKEN_STATUS = "status";

  private static final String LOG_SEND_FAILED =
      "[EmailNotificationService] correo no enviado a {}. Causa: {}";

  private final EmailSenderPort emailSenderPort;

  @Override
  public void notifyUserCreated(final UserCreatedNotificationCommand command) {
    final String template = loadTemplate("user-created.html");
    final String role = command.role() != null ? command.role() : "MEMBER";
    final String body =
        renderTemplate(
            template,
            Map.of(
                TOKEN_NAME, command.name(),
                TOKEN_EMAIL, command.email(),
                TOKEN_PASSWORD, command.password() != null ? command.password() : "",
                TOKEN_ROLE, role));

    final EmailDestinationModel destination =
        new EmailDestinationModel(command.email(), command.name(), SUBJECT_CREATED, body);
    sendOrLog(destination);
  }

  @Override
  public void notifyUserUpdated(final UserUpdatedNotificationCommand command) {
    final String template = loadTemplate("user-updated.html");
    final String role = command.role() != null ? command.role() : "MEMBER";
    final String status = command.status() != null ? command.status() : "ACTIVE";
    final String body =
        renderTemplate(
            template,
            Map.of(
                TOKEN_NAME, command.name(),
                TOKEN_EMAIL, command.email(),
                TOKEN_ROLE, role,
                TOKEN_STATUS, status));

    final EmailDestinationModel destination =
        new EmailDestinationModel(command.email(), command.name(), SUBJECT_UPDATED, body);
    sendOrLog(destination);
  }

  private String loadTemplate(final String templateName) {
    final String path = "/templates/" + templateName;
    try (InputStream inputStream = openResourceStream(path)) {
      if (Objects.isNull(inputStream)) {
        throw EmailSenderException.becauseSendFailed(
            new IllegalStateException("Template not found: " + path));
      }
      return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    } catch (final IOException ioException) {
      throw EmailSenderException.becauseSendFailed(ioException);
    }
  }

  InputStream openResourceStream(final String path) {
    return getClass().getResourceAsStream(path);
  }

  private String renderTemplate(String template, final Map<String, String> values) {
    String result = template;
    for (final Map.Entry<String, String> tokenEntry : values.entrySet()) {
      final String token = "{{" + tokenEntry.getKey() + "}}";
      result = result.replace(token, tokenEntry.getValue());
    }
    return result;
  }

  private void sendOrLog(final EmailDestinationModel destination) {
    try {
      emailSenderPort.send(destination);
      log.info(
          "[EmailNotificationService] Notificación enviada satisfactoriamente a {}",
          destination.getDestinationEmail());
    } catch (final EmailSenderException senderException) {
      log.warn(
          LOG_SEND_FAILED,
          destination.getDestinationEmail(),
          senderException.getMessage(),
          senderException);
      throw senderException;
    }
  }
}
