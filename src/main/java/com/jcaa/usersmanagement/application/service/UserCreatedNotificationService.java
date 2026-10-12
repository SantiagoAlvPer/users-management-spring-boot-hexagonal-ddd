package com.jcaa.usersmanagement.application.service;

import com.jcaa.usersmanagement.application.port.in.NotifyUserCreatedUseCase;
import com.jcaa.usersmanagement.application.port.out.EmailSenderPort;
import com.jcaa.usersmanagement.application.service.dto.command.NotifyUserCreatedCommand;
import com.jcaa.usersmanagement.domain.exception.EmailSenderException;
import com.jcaa.usersmanagement.domain.exception.InvalidNotificationEventException;
import com.jcaa.usersmanagement.domain.model.EmailDestinationModel;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCreatedNotificationService implements NotifyUserCreatedUseCase {

  private static final String SUBJECT_CREATED = "Tu cuenta ha sido creada — Gestion de Usuarios";
  private static final String TEMPLATE_PATH = "/templates/user-created.html";
  private static final String TOKEN_NAME = "name";
  private static final String TOKEN_EMAIL = "email";
  private static final String TOKEN_ROLE = "role";
  private static final String LOG_SENT = "[UserCreatedNotificationService] notificacion enviada. userId={}";

  private final EmailSenderPort emailSenderPort;
  private final Validator validator;

  @Override
  public void execute(final NotifyUserCreatedCommand command) {
    validate(command);
    final String body =
        renderTemplate(
            loadTemplate(),
            Map.of(
                TOKEN_NAME, command.name(),
                TOKEN_EMAIL, command.email(),
                TOKEN_ROLE, command.role()));
    emailSenderPort.send(
        new EmailDestinationModel(command.email(), command.name(), SUBJECT_CREATED, body));
    log.info(LOG_SENT, command.userId());
  }

  private void validate(final NotifyUserCreatedCommand command) {
    if (Objects.isNull(command)) {
      throw InvalidNotificationEventException.becauseEventIsMissing();
    }
    final Set<ConstraintViolation<NotifyUserCreatedCommand>> violations =
        validator.validate(command);
    if (!violations.isEmpty()) {
      throw InvalidNotificationEventException.becauseFieldsAreInvalid(
          violations.stream().map(v -> v.getPropertyPath().toString()).sorted().toList());
    }
  }

  private String loadTemplate() {
    try (InputStream inputStream = openResourceStream(TEMPLATE_PATH)) {
      if (Objects.isNull(inputStream)) {
        throw EmailSenderException.becauseSendFailed(
            new IllegalStateException("Template not found: " + TEMPLATE_PATH));
      }
      return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    } catch (final IOException ioException) {
      throw EmailSenderException.becauseSendFailed(ioException);
    }
  }

  InputStream openResourceStream(final String path) {
    return getClass().getResourceAsStream(path);
  }

  private static String renderTemplate(final String template, final Map<String, String> values) {
    String result = template;
    for (final Map.Entry<String, String> entry : values.entrySet()) {
      result = result.replace("{{" + entry.getKey() + "}}", escapeHtml(entry.getValue()));
    }
    return result;
  }

  private static String escapeHtml(final String value) {
    return value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;");
  }
}
